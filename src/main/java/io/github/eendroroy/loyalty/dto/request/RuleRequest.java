package io.github.eendroroy.loyalty.dto.request;

import io.github.eendroroy.loyalty.enums.RuleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request body for creating or updating a {@link io.github.eendroroy.loyalty.entity.Rule}.
 *
 * <p>The {@code ruleExpression} field contains the custom DSL expression,
 * e.g. {@code WHEN transaction.amount > 50 THEN 100 POINT}.
 */
@Schema(description = "Payload for creating or updating a loyalty rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleRequest {

    @Schema(description = "Human-readable rule name", example = "May Double Points")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Optional description of the rule's purpose",
            example = "Double points for all transactions above £20 during May 2025")
    private String description;

    @Schema(description = "Rule expression in WHEN … THEN … DSL",
            example = "WHEN transaction.amount > 20 AND DATE >= 2025-05-01 THEN 30 POINT")
    @NotBlank(message = "Rule expression is required")
    private String ruleExpression;

    @Schema(description = "Rule lifecycle status: DRAFT, ACTIVE, or INACTIVE", example = "DRAFT")
    @Builder.Default
    private RuleStatus status = RuleStatus.DRAFT;

    @Schema(description = "Evaluation priority — higher value is evaluated first", example = "10")
    @Min(value = 0, message = "Priority must be 0 or greater")
    @Builder.Default
    private Integer priority = 0;

    @Schema(description = "Date from which the rule is valid (inclusive)", example = "2025-05-01")
    private LocalDate activeFrom;

    @Schema(description = "Date until which the rule is valid (inclusive)", example = "2025-05-31")
    private LocalDate activeTo;

    @Schema(description = "Cron expression for scheduled evaluation, e.g. once per hour",
            example = "0 0 * * * *")
    private String frequency;
}
