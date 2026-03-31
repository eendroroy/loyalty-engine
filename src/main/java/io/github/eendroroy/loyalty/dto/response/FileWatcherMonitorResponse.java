package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileWatcherMonitorResponse {
    private Long fileId;
    private Long dataSourceId;
    private String sourceName;
    private String filePath;
    private String description;
    /** Whether the FileWatcherService currently has this path registered. */
    private boolean watching;
    /** Most recent processing attempt for this file config. */
    private FileProcessingLogResponse lastLog;
    private long totalCompleted;
    private long totalFailed;
}

