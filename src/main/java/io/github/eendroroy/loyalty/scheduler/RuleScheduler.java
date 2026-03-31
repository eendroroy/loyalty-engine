package io.github.eendroroy.loyalty.scheduler;

import io.github.eendroroy.loyalty.entity.Rule;
import io.github.eendroroy.loyalty.enums.RuleStatus;
import io.github.eendroroy.loyalty.event.RuleDeletedEvent;
import io.github.eendroroy.loyalty.event.RuleSavedEvent;
import io.github.eendroroy.loyalty.service.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleScheduler {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final RuleService ruleService;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Initialising rule schedules...");
        ruleService.findAllActiveWithFrequency().forEach(this::schedule);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRuleSaved(RuleSavedEvent event) {
        Rule rule = event.rule();
        if (rule.getStatus() == RuleStatus.ACTIVE && rule.getFrequency() != null) {
            schedule(rule);
        } else {
            cancel(rule.getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRuleDeleted(RuleDeletedEvent event) {
        cancel(event.ruleId());
    }

    public void schedule(Rule rule) {
        cancel(rule.getId());
        if (rule.getFrequency() == null) {
            log.warn("Rule '{}' has no frequency; skipping schedule.", rule.getName());
            return;
        }
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> evaluate(rule),
                new CronTrigger(rule.getFrequency())
        );
        scheduledTasks.put(rule.getId(), future);
        log.info("Scheduled rule '{}' with cron '{}'", rule.getName(), rule.getFrequency());
    }

    public void cancel(Long ruleId) {
        ScheduledFuture<?> existing = scheduledTasks.remove(ruleId);
        if (existing != null) {
            existing.cancel(false);
            log.info("Cancelled schedule for rule id={}", ruleId);
        }
    }

    private void evaluate(Rule rule) {
        log.info("Evaluating rule: '{}' — expression: {}", rule.getName(), rule.getRuleExpression());
        // TODO: delegate to RuleEvaluationService once rule engine is implemented
        ruleService.updateLastRunAt(rule.getId());
    }
}

