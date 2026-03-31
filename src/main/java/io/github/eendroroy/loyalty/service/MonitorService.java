package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.dto.response.FileProcessingLogResponse;
import io.github.eendroroy.loyalty.dto.response.FileWatcherMonitorResponse;
import io.github.eendroroy.loyalty.dto.response.WebhookMonitorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MonitorService {
    List<FileWatcherMonitorResponse> getFileWatchers();
    List<WebhookMonitorResponse>     getWebhooks();
    Page<FileProcessingLogResponse>  getLogs(Pageable pageable);
}

