package io.github.eendroroy.loyalty.event;

import io.github.eendroroy.loyalty.enums.RewardType;
import io.github.eendroroy.loyalty.model.IngestedRecord;

/**
 * Published when a rule's condition matches an ingested record and a reward is due.
 *
 * @param ruleId      the matched rule's ID
 * @param ruleName    the matched rule's name
 * @param record      the ingested record that triggered the rule
 * @param rewardType  the type of reward (POINT or VOUCHER)
 * @param rewardValue for POINT: the amount string (e.g. "30");
 *                    for VOUCHER: the voucher code (e.g. "SUMMER25")
 */
public record RuleTriggeredEvent(
        Long ruleId,
        String ruleName,
        IngestedRecord record,
        RewardType rewardType,
        String rewardValue
) {}

