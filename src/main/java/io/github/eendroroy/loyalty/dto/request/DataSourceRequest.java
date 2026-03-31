package io.github.eendroroy.loyalty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body for creating or updating a {@link io.github.eendroroy.loyalty.entity.DataSource}.
 *
 * <p>Field definitions are now nested inside each {@link DataSourceFileRequest}.
 * When a {@code files} list is supplied in a {@code POST} or {@code PUT} request,
 * all file definitions (including their fields) are persisted atomically.
 * On {@code PUT}, supplying {@code files} <em>replaces</em> all existing files and fields;
 * omitting {@code files} (null) leaves existing files untouched.
 */
@Schema(description = "Payload for creating or updating a data source")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceRequest {

    @Schema(description = "Human-readable name for the data source", example = "Transaction Feed")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Optional description of this data source")
    private String description;

    /**
     * Inline file definitions (each with its own field schema).
     * On POST — creates them with the source.
     * On PUT — non-null list <em>replaces</em> all existing files; null leaves them unchanged.
     */
    @Schema(description = "File paths to watch, each with its own field schema. "
            + "On PUT replaces all existing files when non-null.")
    @Valid
    private List<DataSourceFileRequest> files;

    /**
     * Inline webhook config. A data source has exactly one webhook (auto-created, disabled by default).
     * Only the enabled flag is user-controlled; the endpoint path is auto-generated.
     */
    @Schema(description = "Webhook enabled status. The endpoint is auto-generated as /web-hook?dataSourceName={dataSourceName}")
    private DataSourceWebhookRequest webhook;

    /**
     * Schema fields (canonical destination-column definitions).
     * On POST — creates them with the source.
     * On PUT — non-null list <em>replaces</em> all existing schema fields; null leaves them unchanged.
     */
    @Schema(description = "Destination table schema. On PUT replaces all existing schema fields when non-null.")
    @Valid
    private List<DataSourceSchemaFieldRequest> fields;

    @Schema(
            description = "Name of the destination table to store ingested data. "
                    + "The rule engine queries this table to evaluate rules.",
            example = "transaction_events"
    )
    private String destinationTable;
}
