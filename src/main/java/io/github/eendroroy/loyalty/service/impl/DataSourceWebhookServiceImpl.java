package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.DataSourceWebhook;
import io.github.eendroroy.loyalty.repository.DataSourceWebhookRepository;
import io.github.eendroroy.loyalty.service.DataSourceWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataSourceWebhookServiceImpl implements DataSourceWebhookService {

    private final DataSourceWebhookRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceWebhook> findAllByDataSourceId(Long dataSourceId) {
        return repository.findAllWithPropertiesByDataSourceId(dataSourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DataSourceWebhook> findById(Long id) {
        return repository.findByIdWithProperties(id);
    }

    @Override
    @Transactional
    public DataSourceWebhook save(DataSourceWebhook entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteByDataSourceId(Long dataSourceId) {
        repository.deleteByDataSourceId(dataSourceId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
