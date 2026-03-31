package io.github.eendroroy.loyalty.controller;

import io.github.eendroroy.loyalty.dto.response.FileProcessingLogResponse;
import io.github.eendroroy.loyalty.dto.response.FileWatcherMonitorResponse;
import io.github.eendroroy.loyalty.dto.response.WebhookMonitorResponse;
import io.github.eendroroy.loyalty.service.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Monitor", description = "Live status of file watchers, webhooks, and processing history")
@RestController
@RequestMapping("/api/admin/monitors")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @Operation(summary = "File-watcher status",
               description = "Lists every watched file path with live watching status, last processing result, "
                             + "and cumulative success/failure counts.")
    @GetMapping("/file-watchers")
    public List<FileWatcherMonitorResponse> fileWatchers() {
        return monitorService.getFileWatchers();
    }

    @Operation(summary = "Webhook status",
               description = "Lists all configured webhook endpoints with their parent source.")
    @GetMapping("/webhooks")
    public List<WebhookMonitorResponse> webhooks() {
        return monitorService.getWebhooks();
    }

    @Operation(summary = "File processing log",
               description = "Paginated history of all file ingestion attempts sorted newest-first.")
    @GetMapping("/logs")
    public Page<FileProcessingLogResponse> logs(
            @PageableDefault(size = 50, sort = "startedAt") Pageable pageable) {
        return monitorService.getLogs(pageable);
    }
}

