package io.github.eendroroy.loyalty.controller;

import io.github.eendroroy.loyalty.model.DataPullResult;
import io.github.eendroroy.loyalty.service.WebhookIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Inbound webhook ingestion endpoint.
 *
 * <p>External systems POST a JSON payload to the webhook's configured endpoint path
 * (e.g. {@code POST /web-hook?dataSourceName=payments}). The path is matched against the generated
 * endpoint of a registered {@link io.github.eendroroy.loyalty.entity.DataSourceWebhook}.
 *
 * <p>The payload is parsed, type-coerced against the webhook's property schema,
 * written to the destination table (if configured), and published as a
 * {@link io.github.eendroroy.loyalty.event.DataPulledEvent}.
 *
 * <p>This endpoint is intentionally NOT under {@code /api/admin/} — it is meant
 * to be called by external services, not the admin panel.
 * Authentication / HMAC-signature verification will be added in a future release.
 */
@Tag(name = "Webhook Ingestion",
     description = "Inbound HTTP push ingestion — external services POST JSON payloads here. "
             + "Each webhook has an auto-generated unique path (e.g. POST /web-hook?dataSourceName=payments).")
@RestController
@RequiredArgsConstructor
public class WebhookIngestionController {

    private final WebhookIngestionService webhookIngestionService;

    @Operation(
            summary = "Ingest a webhook payload",
            description = "Accepts a JSON object from an external service at the webhook's configured endpoint path. "
                    + "The path must match a registered DataSourceWebhook endpoint exactly "
                    + "(e.g. POST /web-hook?dataSourceName=payments for a data source named 'payments'). "
                    + "Coerces the values against the webhook's property schema, inserts a row into the "
                    + "destination table, and publishes a DataPulledEvent."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payload ingested successfully"),
            @ApiResponse(responseCode = "400", description = "Null or empty payload"),
            @ApiResponse(responseCode = "404", description = "No webhook registered for this data source")
    })
    @PostMapping("/web-hook")
    public ResponseEntity<DataPullResult> ingest(
            HttpServletRequest request,
            @RequestBody Map<String, Object> payload) {

        if (payload == null || payload.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Reconstruct the full endpoint path including query parameters for matching
        var endpoint = request.getRequestURI();
        if (request.getQueryString() != null) {
            endpoint += "?" + request.getQueryString();
        }
        var result = webhookIngestionService.ingestByEndpoint(endpoint, payload);

        if (result.rowsIngested() == 0 && result.errors() > 0) {
            return ResponseEntity.notFound().build();
        }
        if (result.rowsIngested() == 0 && result.rowsSkipped() > 0 && result.errors() == 0) {
            // Webhook is registered but disabled
            return ResponseEntity.status(503).build();
        }
        return ResponseEntity.ok(result);
    }
}
