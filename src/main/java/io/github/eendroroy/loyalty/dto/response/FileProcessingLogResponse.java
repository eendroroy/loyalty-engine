package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.eendroroy.loyalty.enums.FileProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileProcessingLogResponse {
    private Long id;
    private Long dataSourceFileId;
    private String sourceName;
    private String filePath;
    private String fileName;
    private Long fileSizeBytes;
    private Long lastModifiedMillis;
    private FileProcessingStatus status;
    private Long rowsIngested;
    private Long rowsSkipped;
    private Long errors;
    private String instanceId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}

