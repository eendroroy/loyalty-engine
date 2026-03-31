package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.dto.response.FileProcessingLogResponse;
import io.github.eendroroy.loyalty.dto.response.FileWatcherMonitorResponse;
import io.github.eendroroy.loyalty.dto.response.WebhookMonitorResponse;
import io.github.eendroroy.loyalty.enums.FileProcessingStatus;
import io.github.eendroroy.loyalty.repository.DataSourceFileRepository;
import io.github.eendroroy.loyalty.repository.DataSourceWebhookRepository;
import io.github.eendroroy.loyalty.repository.FileProcessingLogRepository;
import io.github.eendroroy.loyalty.service.MonitorService;
import io.github.eendroroy.loyalty.watcher.FileWatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private final DataSourceFileRepository    fileRepository;
    private final DataSourceWebhookRepository webhookRepository;
    private final FileProcessingLogRepository logRepository;
    private final FileWatcherService          fileWatcherService;

    @Override
    @Transactional(readOnly = true)
    public List<FileWatcherMonitorResponse> getFileWatchers() {
        return fileRepository.findAllWithDataSourceAndFields().stream().map(f -> {
            var lastLog = logRepository.findTopByDataSourceFileIdOrderByStartedAtDesc(f.getId())
                    .map(l -> FileProcessingLogResponse.builder()
                            .id(l.getId())
                            .status(l.getStatus())
                            .rowsIngested(l.getRowsIngested())
                            .rowsSkipped(l.getRowsSkipped())
                            .errors(l.getErrors())
                            .instanceId(l.getInstanceId())
                            .startedAt(l.getStartedAt())
                            .completedAt(l.getCompletedAt())
                            .build())
                    .orElse(null);

            return FileWatcherMonitorResponse.builder()
                    .fileId(f.getId())
                    .dataSourceId(f.getDataSource().getId())
                    .sourceName(f.getDataSource().getName())
                    .filePath(f.getFilePath())
                    .description(f.getDescription())
                    .watching(fileWatcherService.isWatching(f.getId()))
                    .lastLog(lastLog)
                    .totalCompleted(logRepository.countByDataSourceFileIdAndStatus(
                            f.getId(), FileProcessingStatus.COMPLETED))
                    .totalFailed(logRepository.countByDataSourceFileIdAndStatus(
                            f.getId(), FileProcessingStatus.FAILED))
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookMonitorResponse> getWebhooks() {
        return webhookRepository.findAllWithDataSource().stream().map(w ->
                WebhookMonitorResponse.builder()
                        .webhookId(w.getId())
                        .dataSourceId(w.getDataSource().getId())
                        .sourceName(w.getDataSource().getName())
                        .endpoint(w.generateEndpoint())
                        .description(w.getDescription())
                        .createdAt(w.getCreatedAt())
                        .build()
        ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileProcessingLogResponse> getLogs(Pageable pageable) {
        return logRepository.findAllWithDetails(pageable).map(l ->
                FileProcessingLogResponse.builder()
                        .id(l.getId())
                        .dataSourceFileId(l.getDataSourceFile().getId())
                        .sourceName(l.getDataSourceFile().getDataSource().getName())
                        .filePath(l.getFilePath())
                        .fileName(l.getFileName())
                        .fileSizeBytes(l.getFileSizeBytes())
                        .lastModifiedMillis(l.getLastModifiedMillis())
                        .status(l.getStatus())
                        .rowsIngested(l.getRowsIngested())
                        .rowsSkipped(l.getRowsSkipped())
                        .errors(l.getErrors())
                        .instanceId(l.getInstanceId())
                        .startedAt(l.getStartedAt())
                        .completedAt(l.getCompletedAt())
                        .build()
        );
    }
}

