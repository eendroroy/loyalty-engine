package io.github.eendroroy.loyalty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a webhook's enabled status.
 *
 * <p>The webhook endpoint path is auto-generated as {@code /web-hook?dataSourceName={dataSourceName}}.
 * Webhook properties are automatically kept in sync with the parent data source's schema fields.
 * Only the enabled/disabled status is user-controlled.
 */
@Schema(description = "Webhook update — enabled flag and optional description")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DataSourceWebhookRequest {

    @Schema(
            description = "Whether this webhook is active (true) or disabled (false). "
                    + "Disabled webhooks reject incoming payloads with 503 Service Unavailable.",
            example = "true"
    )
    private Boolean enabled;

    @Schema(
            description = "Optional description of what this webhook is used for.",
            example = "Receives real-time transaction events from payment gateway"
    )
    private String description;
}
