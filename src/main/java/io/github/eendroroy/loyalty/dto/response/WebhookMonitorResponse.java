package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookMonitorResponse {
    private Long webhookId;
    private Long dataSourceId;
    private String sourceName;
    private String endpoint;
    private String description;
    private LocalDateTime createdAt;
}

