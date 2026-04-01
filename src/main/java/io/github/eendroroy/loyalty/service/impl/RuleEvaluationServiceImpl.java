package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.enums.RewardType;
import io.github.eendroroy.loyalty.enums.RuleStatus;
import io.github.eendroroy.loyalty.event.DataPulledEvent;
import io.github.eendroroy.loyalty.event.RuleTriggeredEvent;
import io.github.eendroroy.loyalty.model.EvaluationResult;
import io.github.eendroroy.loyalty.model.IngestedRecord;
import io.github.eendroroy.loyalty.rule.RuleEvaluator;
import io.github.eendroroy.loyalty.rule.RuleParser;
import io.github.eendroroy.loyalty.rule.ast.ParsedRule;
import io.github.eendroroy.loyalty.rule.exception.RuleParseException;
import io.github.eendroroy.loyalty.service.RuleEvaluationService;
import io.github.eendroroy.loyalty.service.RuleService;
import io.github.eendroroy.loyalty.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens for {@link DataPulledEvent} and evaluates all {@code ACTIVE} rules
 * against each ingested record.
 *
 * <p>For each (rule, record) pair that satisfies the rule's condition:
 * <ul>
 *   <li>A {@link RuleTriggeredEvent} is published for downstream consumers.</li>
 *   <li>If the reward is {@code VOUCHER}, a voucher instance is auto-awarded
 *       (if the voucher is active and has remaining capacity).</li>
 *   <li>If the reward is {@code POINT}, the outcome is logged — point crediting
 *       awaits a future member-management module.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEvaluationServiceImpl implements RuleEvaluationService {

    private final RuleService              ruleService;
    private final RuleParser               ruleParser;
    private final RuleEvaluator            ruleEvaluator;
    private final VoucherService           voucherService;
    private final ApplicationEventPublisher eventPublisher;

    // ── Event listener ────────────────────────────────────────────────────────

    @EventListener
    public void onDataPulled(DataPulledEvent event) {
        if (event.records().isEmpty()) return;
        var results = evaluate(event.records());
        log.debug("Rule evaluation complete: {} outcomes for {} records",
                results.size(), event.records().size());
    }

    // ── Core evaluation ───────────────────────────────────────────────────────

    @Override
    public List<EvaluationResult> evaluate(List<IngestedRecord> records) {
        var activeRules = ruleService.findAllActive();
        if (activeRules.isEmpty()) return List.of();

        var results = new ArrayList<EvaluationResult>();

        for (var rule : activeRules) {
            ParsedRule parsed;
            try {
                parsed = ruleParser.parse(rule.getRuleExpression());
            } catch (RuleParseException e) {
                log.warn("Rule '{}' (id={}) has an invalid expression and was skipped: {}",
                        rule.getName(), rule.getId(), e.getMessage());
                continue;
            }

            for (var record : records) {
                boolean matched;
                try {
                    matched = ruleEvaluator.evaluate(parsed.condition(), record);
                } catch (Exception e) {
                    log.warn("Error evaluating rule '{}' against record from '{}': {}",
                            rule.getName(), record.sourceName(), e.getMessage());
                    continue;
                }

                if (matched) {
                    var outcome = handleReward(rule.getId(), rule.getName(), parsed.reward(), record);
                    results.add(outcome);
                }
            }
        }
        return results;
    }

    // ── Reward fulfilment ─────────────────────────────────────────────────────

    private EvaluationResult handleReward(Long ruleId, String ruleName,
            io.github.eendroroy.loyalty.rule.ast.RewardSpec reward, IngestedRecord record) {

        var rewardValue = reward.value();

        if (reward.type() == RewardType.VOUCHER) {
            rewardValue = awardVoucher(ruleName, reward.value());
        } else {
            log.info("Rule '{}' triggered — awarding {} point(s) for record from '{}'",
                    ruleName, reward.value(), record.sourceName());
        }

        eventPublisher.publishEvent(new RuleTriggeredEvent(
                ruleId, ruleName, record, reward.type(), rewardValue));

        return new EvaluationResult(ruleId, ruleName, record, reward.type(), rewardValue);
    }

    /**
     * Looks up the voucher by code and awards an instance.
     *
     * @return the generated secret code, or the original voucher code if award failed
     */
    private String awardVoucher(String ruleName, String voucherCode) {
        try {
            var voucherOpt = voucherService.findByCode(voucherCode);
            if (voucherOpt.isEmpty()) {
                log.warn("Rule '{}' triggered a VOUCHER reward but no voucher found with code '{}'",
                        ruleName, voucherCode);
                return voucherCode;
            }
            var instance = voucherService.award(voucherOpt.get().getId());
            log.info("Rule '{}' triggered — awarded voucher '{}' with secret code '{}'",
                    ruleName, voucherCode, instance.getSecretCode());
            return instance.getSecretCode();
        } catch (IllegalStateException e) {
            log.warn("Rule '{}' triggered a VOUCHER reward for '{}' but award failed: {}",
                    ruleName, voucherCode, e.getMessage());
            return voucherCode;
        }
    }
}

