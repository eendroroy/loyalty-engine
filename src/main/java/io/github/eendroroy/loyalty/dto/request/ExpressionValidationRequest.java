package io.github.eendroroy.loyalty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Rule expression to validate")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionValidationRequest {

    @Schema(
        description = "The WHEN … THEN … rule expression to validate",
        example = "WHEN transaction.amount > 50 THEN Point(100)"
    )
    @NotBlank(message = "Expression must not be empty")
    private String expression;
}

