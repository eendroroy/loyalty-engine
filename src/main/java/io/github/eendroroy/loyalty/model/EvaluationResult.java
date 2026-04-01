package io.github.eendroroy.loyalty.model;

import io.github.eendroroy.loyalty.enums.RewardType;

/**
 * Summary of a single rule evaluation outcome (one matched record).
 *
 * @param ruleId      ID of the rule that fired
 * @param ruleName    human-readable name of the rule
 * @param record      the ingested record that triggered the rule
 * @param rewardType  POINT or VOUCHER
 * @param rewardValue the point amount, or the voucher code / instance secret code
 */
public record EvaluationResult(
        Long ruleId,
        String ruleName,
        IngestedRecord record,
        RewardType rewardType,
        String rewardValue
) {}

