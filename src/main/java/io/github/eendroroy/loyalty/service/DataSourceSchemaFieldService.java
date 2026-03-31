package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.DataSourceSchemaField;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link DataSourceSchemaField} records — the canonical
 * destination-column schema for a {@link io.github.eendroroy.loyalty.entity.DataSource}.
 */
public interface DataSourceSchemaFieldService {

    /** Returns all schema fields for the given data source, ordered by ID. */
    List<DataSourceSchemaField> findByDataSourceId(Long dataSourceId);

    Optional<DataSourceSchemaField> findById(Long id);

    DataSourceSchemaField save(DataSourceSchemaField entity);

    void deleteById(Long id);
}

