package io.github.eendroroy.loyalty.dto.request;

import io.github.eendroroy.loyalty.enums.RewardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for creating or updating a
 * {@link io.github.eendroroy.loyalty.entity.RuleAction}.
 *
 * <p>A single rule may have multiple actions, e.g. both a POINT award and a VOUCHER.
 */
@Schema(description = "Payload for creating or updating a reward action attached to a rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleActionRequest {

    @Schema(description = "Type of reward to distribute — POINT or VOUCHER", example = "POINT")
    @NotNull(message = "Reward type is required")
    private RewardType rewardType;

    @Schema(description = "Reward amount as a string, e.g. '30' for 30 points or '10.00' for money",
            example = "100")
    @NotBlank(message = "Reward amount is required")
    private String rewardAmount;

    @Schema(description = "Optional description of this action", example = "100 points for May promotion")
    private String description;
}
