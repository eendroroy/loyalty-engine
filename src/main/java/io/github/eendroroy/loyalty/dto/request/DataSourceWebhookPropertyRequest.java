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

@Schema(description = "Typed property definition for an inbound webhook payload")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DataSourceWebhookPropertyRequest {

    @Schema(description = "JSON key in the incoming payload", example = "transactionAmount")
    @NotBlank(message = "Property name is required")
    private String name;

    @Schema(
            description = "Globally unique alias used in rule expressions. "
                    + "Pattern: ^[a-zA-Z][a-zA-Z0-9._]*$",
            example = "transaction.amount"
    )
    @NotBlank(message = "Field alias is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._]*$",
             message = "Alias must match ^[a-zA-Z][a-zA-Z0-9._]*$")
    private String fieldAlias;

    @Schema(description = "Data type of this property's values", example = "DECIMAL")
    @NotNull(message = "Data type is required")
    private FieldDataType dataType;

    @Schema(
            description = "Date format pattern used when dataType is DATE. "
                    + "Defaults to ISO-8601 (yyyy-MM-dd) if blank.",
            example = "yyyy-MM-dd"
    )
    private String format;

    @Schema(
            description = "Optional description shown as a hint in the rule-expression editor",
            example = "Transaction value in the customer's local currency"
    )
    private String description;
}

