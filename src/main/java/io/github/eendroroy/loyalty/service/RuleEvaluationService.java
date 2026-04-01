package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.model.EvaluationResult;
import io.github.eendroroy.loyalty.model.IngestedRecord;

import java.util.List;

/**
 * Evaluates all active rules against a batch of ingested records.
 *
 * <p>For each record that satisfies a rule's condition, the corresponding
 * reward is issued (POINT logged / VOUCHER instance auto-awarded) and a
 * {@link io.github.eendroroy.loyalty.event.RuleTriggeredEvent} is published.
 */
public interface RuleEvaluationService {

    /**
     * Evaluates all active rules against the supplied records.
     *
     * @param records the batch of newly ingested records
     * @return list of outcomes — one entry per (rule, record) pair that matched
     */
    List<EvaluationResult> evaluate(List<IngestedRecord> records);
}

