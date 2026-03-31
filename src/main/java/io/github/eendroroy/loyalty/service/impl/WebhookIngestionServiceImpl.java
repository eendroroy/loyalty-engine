package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.DataSourceField;
import io.github.eendroroy.loyalty.entity.DataSourceWebhook;
import io.github.eendroroy.loyalty.entity.DataSourceWebhookProperty;
import io.github.eendroroy.loyalty.enums.FieldDataType;
import io.github.eendroroy.loyalty.event.DataPulledEvent;
import io.github.eendroroy.loyalty.model.DataPullResult;
import io.github.eendroroy.loyalty.model.IngestedRecord;
import io.github.eendroroy.loyalty.repository.DataSourceWebhookRepository;
import io.github.eendroroy.loyalty.service.DataSourceTableService;
import io.github.eendroroy.loyalty.service.WebhookIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ingests a single JSON payload pushed to a webhook endpoint.
 *
 * <h3>Processing flow</h3>
 * <ol>
 *   <li>Load the {@code DataSourceWebhook} with its properties and parent {@code DataSource}.</li>
 *   <li>For each registered {@link DataSourceWebhookProperty}, look up the value in the payload
 *       by {@code name} and type-coerce it using {@code dataType} (and {@code format} for dates).</li>
 *   <li>Create/ensure the destination table exists using the property schema as synthetic fields.</li>
 *   <li>Insert the coerced row into the destination table.</li>
 *   <li>Publish a {@link DataPulledEvent} so downstream listeners (e.g. a future
 *       {@code RuleEvaluationService}) can react.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookIngestionServiceImpl implements WebhookIngestionService {

    private final DataSourceWebhookRepository webhookRepository;
    private final DataSourceTableService tableService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DataPullResult ingest(Long webhookId, Map<String, Object> payload) {
        var webhook = webhookRepository.findByIdWithDataSourceAndProperties(webhookId).orElse(null);
        if (webhook == null) {
            log.warn("Webhook id={} not found — skipping ingestion.", webhookId);
            return DataPullResult.failed();
        }
        return doIngest(webhook, payload);
    }

    @Override
    @Transactional
    public DataPullResult ingestByEndpoint(String endpoint, Map<String, Object> payload) {
        var webhook = webhookRepository.findByEndpointWithDataSourceAndProperties(endpoint).orElse(null);
        if (webhook == null) {
            log.warn("No webhook found for endpoint='{}' — skipping ingestion.", endpoint);
            return DataPullResult.failed();
        }
        return doIngest(webhook, payload);
    }

    /** Core ingestion logic shared by both {@link #ingest} and {@link #ingestByEndpoint}. */
    private DataPullResult doIngest(DataSourceWebhook webhook, Map<String, Object> payload) {

        if (!webhook.isEnabled()) {
            log.info("Webhook id={} is disabled — payload rejected.", webhook.getId());
            return new DataPullResult(0, 1, 0);
        }

        var ds = webhook.getDataSource();
        var properties = ds.getSchemaFields();  // Use schema fields as properties

        if (properties == null || properties.isEmpty()) {
            log.warn("Webhook id={} has no schema field definitions — nothing to ingest.", webhook.getId());
            return new DataPullResult(0, 0, 0);
        }

        if (payload == null || payload.isEmpty()) {
            log.warn("Webhook id={} received empty payload — skipping.", webhook.getId());
            return new DataPullResult(0, 1, 0);
        }

        // Build synthetic DataSourceField list from schema fields (webhook properties)
        var syntheticFields = properties.stream().map(this::schemaFieldToDataSourceField).toList();

        // Coerce payload values into a typed row
        var rowData = new LinkedHashMap<String, Object>();
        long skipped = 0;
        for (var schemaField : properties) {
            var raw = payload.get(schemaField.getName());  // JSON key = schema field name
            if (raw == null) {
                skipped++;
                continue;
            }
            try {
                // For webhooks, schema field name is both the JSON key and the destination column name
                rowData.put(schemaField.getName(), coerce(raw.toString(), schemaField.getDataType(), null));
            } catch (Exception e) {
                log.warn("Webhook id={} failed to coerce property '{}': {}",
                        webhook.getId(), schemaField.getName(), e.getMessage());
                return new DataPullResult(0, 0, 1);
            }
        }

        if (rowData.isEmpty()) {
            log.info("Webhook id={} payload contained no matching properties — skipped.", webhook.getId());
            return new DataPullResult(0, 1, 0);
        }

        // Ensure destination table and insert
        if (ds.getDestinationTable() != null && !ds.getDestinationTable().isBlank()) {
            tableService.createDestinationTableIfNotExists(ds, syntheticFields);
            var record = new IngestedRecord(ds.getId(), ds.getName(), LocalDateTime.now(), rowData);
            tableService.insertRecords(ds, List.of(record), syntheticFields);
            eventPublisher.publishEvent(new DataPulledEvent(List.of(record)));
            log.info("Webhook id={} ingested 1 record into '{}' ({} skipped fields)",
                    webhook.getId(), ds.getDestinationTable(), skipped);
            return new DataPullResult(1, skipped, 0);
        }

        // No destination table — still publish event
        var record = new IngestedRecord(ds.getId(), ds.getName(), LocalDateTime.now(), rowData);
        eventPublisher.publishEvent(new DataPulledEvent(List.of(record)));
        log.info("Webhook id={} processed 1 record (no destination table configured)", webhook.getId());
        return new DataPullResult(1, skipped, 0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Converts a {@link io.github.eendroroy.loyalty.entity.DataSourceSchemaField} into a transient (not-persisted)
     * {@link DataSourceField} suitable for passing to {@link DataSourceTableService}.
     * For webhooks, the schema field name serves as both the JSON key and the destination column name.
     */
    private DataSourceField schemaFieldToDataSourceField(io.github.eendroroy.loyalty.entity.DataSourceSchemaField schemaField) {
        var field = new DataSourceField();
        field.setFieldName(schemaField.getName());      // JSON key in webhook payload
        field.setFieldAlias(schemaField.getName());     // Destination column name (same as field name for webhooks)
        field.setDataType(schemaField.getDataType());
        field.setDescription(schemaField.getDescription());
        return field;
    }

    /** Type-coerces a raw string value according to the declared {@link FieldDataType}. */
    private Object coerce(String raw, FieldDataType type, String format) {
        if (raw == null || raw.isBlank()) return null;
        var v = raw.trim();
        return switch (type) {
            case INTEGER -> Long.parseLong(v);
            case DECIMAL -> new BigDecimal(v);
            case DATE -> (format != null && !format.isBlank())
                    ? LocalDate.parse(v, DateTimeFormatter.ofPattern(format))
                    : LocalDate.parse(v);
            case BOOLEAN -> Boolean.parseBoolean(v);
            case STRING  -> v;
        };
    }
}
