package io.github.eendroroy.loyalty.dto.request;

import io.github.eendroroy.loyalty.enums.FieldDataType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for creating or updating a single
 * {@link io.github.eendroroy.loyalty.entity.DataSourceField}.
 *
 * <p>{@code fieldAlias} must be globally unique across all data sources and conform to
 * the identifier pattern {@code ^[a-zA-Z][a-zA-Z0-9._]*$}.
 * It is the token used in rule expressions (e.g. {@code transaction.amount}).
 */
@Schema(description = "Payload for creating or updating a data source field definition")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceFieldRequest {

    @Schema(description = "Column header or raw field key as it appears in the source",
            example = "Amount")
    @NotBlank(message = "Field name is required")
    private String fieldName;

    @Schema(description = "Unique alias used in rule expressions. Must match ^[a-zA-Z][a-zA-Z0-9._]*$",
            example = "transaction.amount")
    @NotBlank(message = "Field alias is required")
    @Pattern(
        regexp = "^[a-zA-Z][a-zA-Z0-9._]*$",
        message = "Field alias must be a valid identifier (e.g. transaction.amount)"
    )
    private String fieldAlias;

    @Schema(description = "1-based column index; applicable for FILE type sources only", example = "3")
    private Integer columnNumber; // 1-based column index; applicable for FILE type

    @Schema(description = "Data type of the field value", example = "DECIMAL")
    @NotNull(message = "Data type is required")
    private FieldDataType dataType;

    @Schema(description = "Optional human-readable description shown in rule-expression hints",
            example = "Transaction amount in base currency")
    private String description;
}
