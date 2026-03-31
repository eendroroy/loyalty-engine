package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.model.DataPullResult;

import java.nio.file.Path;

/**
 * Service for pulling and ingesting data from a configured
 * {@link io.github.eendroroy.loyalty.entity.DataSource}.
 *
 * <p>Both overloads load the data source (with its field definitions) from the
 * database inside a read-only transaction, parse the file, type-coerce every
 * field value according to its declared {@link io.github.eendroroy.loyalty.entity.FieldDataType},
 * and publish a {@link io.github.eendroroy.loyalty.event.DataPulledEvent} for each
 * batch of ingested records.
 *
 * <p>Only {@link io.github.eendroroy.loyalty.entity.DataSourceType#FILE} sources
 * are active; calling either method for a
 * {@link io.github.eendroroy.loyalty.entity.DataSourceType#WEBHOOK} source is a
 * no-op (WEBHOOK data arrives via inbound HTTP push, not by polling).
 */
public interface DataPullService {

    /**
     * Pulls data from the source's configured {@code location} path.
     * Used by {@link io.github.eendroroy.loyalty.scheduler.DataSourceScheduler}
     * on its cron schedule.
     *
     * @param dataSourceFileId primary key of the data source to pull
     * @return ingestion summary
     */
    DataPullResult pull(Long dataSourceFileId);

    /**
     * Pulls data from a specific file path.
     * Used by {@link io.github.eendroroy.loyalty.watcher.FileWatcherService}
     * when a file-system change event fires for the watched directory.
     * If {@code filePath} is {@code null}, falls back to the source's configured location.
     *
     * @param dataSourceFileId primary key of the data source to pull
     * @param filePath         concrete file to read, or {@code null} to use the source location
     * @return ingestion summary
     */
    DataPullResult pull(Long dataSourceFileId, Path filePath);
}
