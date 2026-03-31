package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.model.DataPullResult;

import java.util.Map;

/**
 * Service that ingests a single payload pushed to a webhook endpoint.
 *
 * <p>Looks up the {@link io.github.eendroroy.loyalty.entity.DataSourceWebhook} by
 * {@code webhookId}, coerces each value according to the registered property definitions,
 * writes the row to the destination table, and publishes a
 * {@link io.github.eendroroy.loyalty.event.DataPulledEvent}.
 */
public interface WebhookIngestionService {

    /**
     * Ingests a single payload from an external push event by webhook primary key.
     *
     * @param webhookId the primary key of the target {@code DataSourceWebhook}
     * @param payload   the parsed request body (JSON key → raw value map)
     * @return a summary of the ingestion (rowsIngested, rowsSkipped, errors)
     */
    DataPullResult ingest(Long webhookId, Map<String, Object> payload);

    /**
     * Ingests a single payload from an external push event by endpoint path.
     *
     * @param endpoint the registered endpoint path of the target {@code DataSourceWebhook}
     *                 (e.g. {@code /hooks/payments})
     * @param payload  the parsed request body (JSON key → raw value map)
     * @return a summary of the ingestion (rowsIngested, rowsSkipped, errors)
     */
    DataPullResult ingestByEndpoint(String endpoint, Map<String, Object> payload);
}

