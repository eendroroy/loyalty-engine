package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.RuleAction;
import io.github.eendroroy.loyalty.repository.RuleActionRepository;
import io.github.eendroroy.loyalty.service.RuleActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RuleActionServiceImpl implements RuleActionService {

    private final RuleActionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<RuleAction> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleAction> findAll(Specification<RuleAction> spec) {
        return repository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleAction> findByRuleId(Long ruleId) {
        return repository.findByRuleId(ruleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RuleAction> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public RuleAction save(RuleAction entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
