package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.DataSourceSchemaField;
import io.github.eendroroy.loyalty.repository.DataSourceSchemaFieldRepository;
import io.github.eendroroy.loyalty.service.DataSourceSchemaFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataSourceSchemaFieldServiceImpl implements DataSourceSchemaFieldService {

    private final DataSourceSchemaFieldRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceSchemaField> findByDataSourceId(Long dataSourceId) {
        return repository.findByDataSourceIdOrderById(dataSourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DataSourceSchemaField> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public DataSourceSchemaField save(DataSourceSchemaField entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

