package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.DataSourceFile;
import io.github.eendroroy.loyalty.entity.FileProcessingLog;
import io.github.eendroroy.loyalty.model.DataPullResult;

/**
 * Manages {@link FileProcessingLog} lifecycle for distributed deduplication.
 *
 * <p>Both methods run in {@code REQUIRES_NEW} transactions so that a constraint
 * violation on the INSERT (dedup race) rolls back only the claim attempt and
 * never the outer pull call.
 */
public interface FileProcessingLogService {

    /**
     * Deletes any previous {@code FAILED} log entry for the given file version,
     * then attempts to insert an {@code IN_PROGRESS} claim row.
     *
     * @return the persisted log entry, or {@code null} if the file version was
     *         already claimed or completed by another instance
     */
    FileProcessingLog claimFileProcessing(DataSourceFile fileConfig, String filePath,
                                          long fileSize, long lastModified);

    /**
     * Updates the log row identified by {@code logId} to {@code COMPLETED} or
     * {@code FAILED} based on the {@link DataPullResult}.
     */
    void finaliseLog(Long logId, DataPullResult result);
}

