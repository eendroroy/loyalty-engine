package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.Rule;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface RuleService {
    List<Rule> findAll();
    List<Rule> findAll(Specification<Rule> spec);
    Optional<Rule> findById(Long id);
    Rule save(Rule entity);
    void deleteById(Long id);

    /** ACTIVE rules that have a non-null frequency (cron expression). */
    List<Rule> findAllActiveWithFrequency();

    /** All rules with {@code ACTIVE} status — used by the rule evaluation engine. */
    List<Rule> findAllActive();

    /** Update lastRunAt timestamp after a scheduled evaluation. */
    void updateLastRunAt(Long id);
}
