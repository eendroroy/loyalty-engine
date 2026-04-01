package io.github.eendroroy.loyalty.rule.ast;

import io.github.eendroroy.loyalty.enums.RewardType;

/**
 * The reward portion of a parsed rule expression.
 *
 * <p>Examples in DSL:
 * <ul>
 *   <li>{@code Point(30)}    → {@code RewardSpec(POINT, "30")}</li>
 *   <li>{@code Voucher(SUMMER25)} → {@code RewardSpec(VOUCHER, "SUMMER25")}</li>
 * </ul>
 *
 * @param type  the reward category (POINT or VOUCHER)
 * @param value the amount for POINT rewards, or the voucher code for VOUCHER rewards
 */
public record RewardSpec(RewardType type, String value) {
}

