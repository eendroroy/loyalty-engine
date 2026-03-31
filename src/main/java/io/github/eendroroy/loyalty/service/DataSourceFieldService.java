package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.DataSourceField;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing individual {@link DataSourceField} entities.
 */
public interface DataSourceFieldService {

    /**
     * Returns all fields across all data sources.
     *
     * @return list of all field definitions
     */
    List<DataSourceField> findAll();

    /**
     * Returns all fields matching the given JPA specification.
     *
     * @param spec the JPA {@link Specification} predicate
     * @return matching field definitions
     */
    List<DataSourceField> findAll(Specification<DataSourceField> spec);

    /** Returns all fields belonging to a specific file. */
    List<DataSourceField> findByDataSourceFileId(Long dataSourceFileId);

    /** Returns all fields belonging to any file of the given data source. */
    List<DataSourceField> findByDataSourceId(Long dataSourceId);

    /**
     * Retrieves a single field by its primary key.
     *
     * @param id the field primary key
     * @return the field wrapped in an {@link Optional}, or empty if not found
     */
    Optional<DataSourceField> findById(Long id);

    /**
     * Persists a new or updated field definition.
     *
     * @param entity the field to persist
     * @return the managed, saved entity
     */
    DataSourceField save(DataSourceField entity);

    /**
     * Deletes the field with the given ID.
     *
     * @param id the field primary key
     */
    void deleteById(Long id);
}
