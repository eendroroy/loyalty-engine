package io.github.eendroroy.loyalty.event;

import io.github.eendroroy.loyalty.model.IngestedRecord;

import java.util.List;

/**
 * Published by {@link io.github.eendroroy.loyalty.service.DataPullService}
 * after successfully ingesting one or more records from a data source.
 *
 * <p>Listeners (e.g. the future {@code RuleEvaluationService}) should subscribe
 * with {@code @EventListener} or, if database writes are needed, with
 * {@code @TransactionalEventListener(AFTER_COMMIT)}.
 *
 * @param records the batch of ingested rows; never null, never empty
 */
public record DataPulledEvent(List<IngestedRecord> records) {
}

