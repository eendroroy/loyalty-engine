package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.DataSourceField;
import io.github.eendroroy.loyalty.entity.DataSourceFile;
import io.github.eendroroy.loyalty.enums.FieldDataType;
import io.github.eendroroy.loyalty.event.DataPulledEvent;
import io.github.eendroroy.loyalty.model.DataPullResult;
import io.github.eendroroy.loyalty.model.IngestedRecord;
import io.github.eendroroy.loyalty.repository.DataSourceFileRepository;
import io.github.eendroroy.loyalty.service.DataPullService;
import io.github.eendroroy.loyalty.service.DataSourceTableService;
import io.github.eendroroy.loyalty.service.FileProcessingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Ingests data from {@link DataSourceFile} entries.
 *
 * <h3>Distributed deduplication</h3>
 * Before parsing, an {@code IN_PROGRESS} row is inserted into {@code file_processing_log}
 * with a unique constraint on {@code (data_source_file_id, file_path, last_modified_millis,
 * file_size_bytes)}.  If another instance races to process the same file version, its
 * INSERT will fail with a constraint violation and it silently skips.
 * After parsing, the log row is updated to {@code COMPLETED} or {@code FAILED}.
 * {@code FAILED} rows are deleted on the next trigger so the file is retried.
 *
 * <h3>CSV parsing strategy</h3>
 * Fields with {@code columnNumber} are resolved by 1-based index.
 * Fields without {@code columnNumber} are matched case-insensitively by {@code fieldName}
 * against the CSV header row.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataPullServiceImpl implements DataPullService {

    private final DataSourceFileRepository fileRepository;
    private final FileProcessingLogService fileProcessingLogService;
    private final DataSourceTableService tableService;
    private final ApplicationEventPublisher eventPublisher;

    // ── Public API ────────────────────────────────────────────────────────────

    @Override
    public DataPullResult pull(Long dataSourceFileId) {
        return pull(dataSourceFileId, null);
    }

    @Override
    public DataPullResult pull(Long dataSourceFileId, Path filePath) {
        var fileConfig = fileRepository.findByIdWithDataSourceAndFields(dataSourceFileId).orElse(null);
        if (fileConfig == null) {
            log.error("DataSourceFile id={} not found; skipping pull.", dataSourceFileId);
            return DataPullResult.failed();
        }

        var actualPath = filePath != null ? filePath : Paths.get(fileConfig.getFilePath());

        // Compute file metadata for dedup key
        long fileSize, lastModified;
        try {
            fileSize    = Files.size(actualPath);
            lastModified = Files.getLastModifiedTime(actualPath).toMillis();
        } catch (IOException e) {
            log.error("Cannot read file metadata for '{}': {}", actualPath, e.getMessage());
            return DataPullResult.failed();
        }

        // Try to claim this file version
        var logEntry = fileProcessingLogService.claimFileProcessing(
                fileConfig, actualPath.toString(), fileSize, lastModified);
        if (logEntry == null) {
            log.info("File '{}' (size={}, modified={}) already claimed or completed — skipping.",
                    actualPath.getFileName(), fileSize, lastModified);
            return new DataPullResult(0, 0, 0);
        }

        // Parse
        var result = parseFile(fileConfig, actualPath);

        // Finalise the log row
        fileProcessingLogService.finaliseLog(logEntry.getId(), result);


        return result;
    }


    // ── CSV parsing ───────────────────────────────────────────────────────────

    private DataPullResult parseFile(DataSourceFile fileConfig, Path filePath) {
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            log.warn("File not found or not a regular file: {}", filePath);
            return DataPullResult.failed();
        }

        var ds     = fileConfig.getDataSource();
        var fields = fileConfig.getFields();
        if (fields == null || fields.isEmpty()) {
            log.warn("No field definitions for file '{}' in source '{}'; skipping.",
                    filePath.getFileName(), ds.getName());
            return new DataPullResult(0, 0, 0);
        }

        // Ensure destination table exists if configured
        if (ds.getDestinationTable() != null && !ds.getDestinationTable().isBlank()) {
            tableService.createDestinationTableIfNotExists(ds, fileConfig.getFields());
        }

        var indexedFields = fields.stream()
                .filter(f -> f.getColumnNumber() != null)
                .sorted(Comparator.comparingInt(DataSourceField::getColumnNumber))
                .toList();
        var namedFields = fields.stream().filter(f -> f.getColumnNumber() == null).toList();

        char delimiter = fileConfig.getFieldSeparator() != null && !fileConfig.getFieldSeparator().isEmpty()
                ? fileConfig.getFieldSeparator().charAt(0) : ',';
        Character quote = fileConfig.getQuoteCharacter() != null && !fileConfig.getQuoteCharacter().isEmpty()
                ? fileConfig.getQuoteCharacter().charAt(0) : '"';
        int skipLines = fileConfig.getSkipFirstNLines() != null ? fileConfig.getSkipFirstNLines() : 0;

        var format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setQuote(quote)
                .setHeader().setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true).setIgnoreEmptyLines(true).setTrim(true)
                .build();

        long ingested = 0, skipped = 0, errors = 0;
        var records = new ArrayList<IngestedRecord>();

        try (var reader = Files.newBufferedReader(filePath)) {
            // Skip leading non-header lines (e.g. preamble metadata rows)
            for (int i = 0; i < skipLines; i++) {
                if (reader.readLine() == null) {
                    log.warn("File '{}' has fewer lines than skipFirstNLines={}", filePath.getFileName(), skipLines);
                    return new DataPullResult(0, 0, 0);
                }
            }
            try (var parser = format.parse(reader)) {
                for (var row : parser) {
                    try {
                        var rowData  = new LinkedHashMap<String, Object>();
                        var hasValue = false;

                        for (var field : indexedFields) {
                            int idx = field.getColumnNumber() - 1;
                            if (idx < row.size()) {
                                var raw = row.get(idx);
                                rowData.put(field.getFieldAlias(), coerce(raw, field.getDataType()));
                                if (raw != null && !raw.isBlank()) hasValue = true;
                            }
                        }
                        for (var field : namedFields) {
                            try {
                                var raw = row.get(field.getFieldName());
                                rowData.put(field.getFieldAlias(), coerce(raw, field.getDataType()));
                                if (raw != null && !raw.isBlank()) hasValue = true;
                            } catch (IllegalArgumentException ignored) { /* header absent */ }
                        }

                        if (!hasValue) { skipped++; continue; }
                        records.add(new IngestedRecord(ds.getId(), ds.getName(), LocalDateTime.now(), rowData));
                        ingested++;
                    } catch (Exception e) {
                        log.warn("Error parsing row {} in '{}': {}",
                                parser.getCurrentLineNumber(), ds.getName(), e.getMessage());
                        errors++;
                    }
                }
            }

        } catch (IOException e) {
            log.error("Failed to read '{}': {}", filePath, e.getMessage(), e);
            return DataPullResult.failed();
        }

        // Insert records into destination table
        if (!records.isEmpty() && ds.getDestinationTable() != null && !ds.getDestinationTable().isBlank()) {
            tableService.insertRecords(ds, records, fileConfig.getFields());
        }

        if (!records.isEmpty()) {
            eventPublisher.publishEvent(new DataPulledEvent(List.copyOf(records)));
        }

        log.info("Pull complete '{}' ({}): {} ingested, {} skipped, {} errors",
                ds.getName(), filePath.getFileName(), ingested, skipped, errors);
        return new DataPullResult(ingested, skipped, errors);
    }

    // ── Type coercion ─────────────────────────────────────────────────────────

    private Object coerce(String raw, FieldDataType type) {
        if (raw == null || raw.isBlank()) return null;
        var v = raw.trim();
        return switch (type) {
            case INTEGER -> Long.parseLong(v);
            case DECIMAL -> new BigDecimal(v);
            case DATE    -> LocalDate.parse(v);
            case BOOLEAN -> Boolean.parseBoolean(v);
            case STRING  -> v;
        };
    }
}

