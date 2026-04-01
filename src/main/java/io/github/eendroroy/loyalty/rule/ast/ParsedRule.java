package io.github.eendroroy.loyalty.rule.ast;

/**
 * The fully parsed representation of a {@code WHEN … THEN …} rule expression.
 *
 * @param condition the logical condition tree (root node)
 * @param reward    the reward to issue when the condition evaluates to {@code true}
 */
public record ParsedRule(LogicalNode condition, RewardSpec reward) {
}

