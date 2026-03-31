package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.DataSourceField;
import io.github.eendroroy.loyalty.repository.DataSourceFieldRepository;
import io.github.eendroroy.loyalty.service.DataSourceFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataSourceFieldServiceImpl implements DataSourceFieldService {

    private final DataSourceFieldRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceField> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceField> findAll(Specification<DataSourceField> spec) {
        return repository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceField> findByDataSourceFileId(Long dataSourceFileId) {
        return repository.findByDataSourceFileId(dataSourceFileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceField> findByDataSourceId(Long dataSourceId) {
        return repository.findByDataSourceId(dataSourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DataSourceField> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public DataSourceField save(DataSourceField entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
