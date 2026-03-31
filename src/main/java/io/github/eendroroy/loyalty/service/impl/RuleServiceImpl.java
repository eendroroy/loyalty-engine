package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.Rule;
import io.github.eendroroy.loyalty.enums.RuleStatus;
import io.github.eendroroy.loyalty.event.RuleDeletedEvent;
import io.github.eendroroy.loyalty.event.RuleSavedEvent;
import io.github.eendroroy.loyalty.repository.RuleRepository;
import io.github.eendroroy.loyalty.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final RuleRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<Rule> findAll() {
        return repository.findAllWithActions();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rule> findAll(Specification<Rule> spec) {
        return repository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rule> findById(Long id) {
        return repository.findByIdWithActions(id);
    }

    @Override
    @Transactional
    public Rule save(Rule entity) {
        Rule saved = repository.save(entity);
        eventPublisher.publishEvent(new RuleSavedEvent(saved));
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
        eventPublisher.publishEvent(new RuleDeletedEvent(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rule> findAllActiveWithFrequency() {
        return repository.findByStatusAndFrequencyIsNotNull(RuleStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void updateLastRunAt(Long id) {
        repository.updateLastRunAt(id, LocalDateTime.now());
    }
}
