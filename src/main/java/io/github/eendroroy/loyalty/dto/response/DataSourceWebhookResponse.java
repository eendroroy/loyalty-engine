package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a webhook. The endpoint path is auto-generated from the parent
 * data source name. Properties are the same as the parent data source's schema fields
 * (kept in sync automatically).
 */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSourceWebhookResponse {
    private Long id;
    private String endpoint;              // auto-generated: /web-hook?dataSourceName={dataSourceName}
    private boolean enabled;              // true = active, false = disabled
    private String description;
    private List<DataSourceSchemaFieldResponse> properties;  // mirrors schema fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
