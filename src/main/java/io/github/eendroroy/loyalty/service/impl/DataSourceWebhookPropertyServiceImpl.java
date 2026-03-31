package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.DataSourceWebhookProperty;
import io.github.eendroroy.loyalty.repository.DataSourceWebhookPropertyRepository;
import io.github.eendroroy.loyalty.service.DataSourceWebhookPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataSourceWebhookPropertyServiceImpl implements DataSourceWebhookPropertyService {

    private final DataSourceWebhookPropertyRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceWebhookProperty> findAllByWebhookId(Long webhookId) {
        return repository.findAllByWebhookId(webhookId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DataSourceWebhookProperty> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public DataSourceWebhookProperty save(DataSourceWebhookProperty entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteByWebhookId(Long webhookId) {
        repository.deleteByWebhookId(webhookId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

