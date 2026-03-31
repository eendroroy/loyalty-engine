package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.RuleAction;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface RuleActionService {
    List<RuleAction> findAll();
    List<RuleAction> findAll(Specification<RuleAction> spec);
    List<RuleAction> findByRuleId(Long ruleId);
    Optional<RuleAction> findById(Long id);
    RuleAction save(RuleAction entity);
    void deleteById(Long id);
}
