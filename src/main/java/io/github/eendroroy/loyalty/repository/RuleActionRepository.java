package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.RuleAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RuleActionRepository extends JpaRepository<RuleAction, Long>, JpaSpecificationExecutor<RuleAction> {
    List<RuleAction> findByRuleId(Long ruleId);
}
