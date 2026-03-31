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
 * Request body for creating or updating a
 * {@link io.github.eendroroy.loyalty.entity.DataSourceSchemaField}.
 *
 * <p>{@code name} must conform to the identifier pattern {@code ^[a-zA-Z][a-zA-Z0-9._]*$}
 * and must be unique within the owning data source.
 * It becomes the destination table column name and the rule-expression token.
 */
@Schema(description = "Payload for creating or updating a data source schema field (destination column)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceSchemaFieldRequest {

    @Schema(
            description = "Destination column name and rule-expression token. "
                    + "Unique within the data source. Must match ^[a-zA-Z][a-zA-Z0-9._]*$",
            example = "amount"
    )
    @NotBlank(message = "Field name is required")
    @Pattern(
            regexp = "^[a-zA-Z][a-zA-Z0-9._]*$",
            message = "Name must be a valid identifier (e.g. amount, transaction.amount)"
    )
    private String name;

    @Schema(
            description = "Optional human-readable description shown in rule-expression hints",
            example = "Transaction amount in base currency"
    )
    private String description;

    @Schema(description = "Target data type for the destination table column", example = "DECIMAL")
    @NotNull(message = "Data type is required")
    private FieldDataType dataType;
}

