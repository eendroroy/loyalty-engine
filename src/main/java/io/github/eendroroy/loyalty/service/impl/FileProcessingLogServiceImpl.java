package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.DataSourceFile;
import io.github.eendroroy.loyalty.entity.FileProcessingLog;
import io.github.eendroroy.loyalty.enums.FileProcessingStatus;
import io.github.eendroroy.loyalty.model.DataPullResult;
import io.github.eendroroy.loyalty.repository.FileProcessingLogRepository;
import io.github.eendroroy.loyalty.service.FileProcessingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingLogServiceImpl implements FileProcessingLogService {

    private static final String INSTANCE_ID = ManagementFactory.getRuntimeMXBean().getName();

    private final FileProcessingLogRepository logRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessingLog claimFileProcessing(DataSourceFile fileConfig, String filePath,
                                                  long fileSize, long lastModified) {
        // Delete any previous FAILED entry so the file can be retried
        logRepository.deleteFailedEntry(fileConfig.getId(), filePath, lastModified, fileSize);

        var existing = logRepository
                .findByDataSourceFileIdAndFilePathAndLastModifiedMillisAndFileSizeBytes(
                        fileConfig.getId(), filePath, lastModified, fileSize);
        if (existing.isPresent()) return null; // COMPLETED or IN_PROGRESS by another instance

        var entry = new FileProcessingLog();
        entry.setDataSourceFile(fileConfig);
        entry.setFilePath(filePath);
        entry.setFileName(Paths.get(filePath).getFileName().toString());
        entry.setFileSizeBytes(fileSize);
        entry.setLastModifiedMillis(lastModified);
        entry.setStatus(FileProcessingStatus.IN_PROGRESS);
        entry.setInstanceId(INSTANCE_ID);
        entry.setStartedAt(LocalDateTime.now());
        try {
            return logRepository.saveAndFlush(entry);
        } catch (DataIntegrityViolationException e) {
            return null; // lost the race — another instance claimed it first
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finaliseLog(Long logId, DataPullResult result) {
        logRepository.findById(logId).ifPresent(entry -> {
            entry.setStatus(result.errors() > 0 && result.rowsIngested() == 0
                    ? FileProcessingStatus.FAILED : FileProcessingStatus.COMPLETED);
            entry.setRowsIngested(result.rowsIngested());
            entry.setRowsSkipped(result.rowsSkipped());
            entry.setErrors(result.errors());
            entry.setCompletedAt(LocalDateTime.now());
            logRepository.save(entry);
        });
    }
}

