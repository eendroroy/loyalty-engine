package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.DataSource;
import io.github.eendroroy.loyalty.entity.DataSourceFile;
import io.github.eendroroy.loyalty.entity.DataSourceSchemaField;
import io.github.eendroroy.loyalty.entity.DataSourceWebhook;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service for managing {@link DataSource} aggregates.
 * All reads are in a {@code readOnly} transaction; writes are in full transactions
 * and publish domain events after commit.
 */
public interface DataSourceService {

    /**
     * Returns all data sources with a configured (non-null, non-blank)
     * {@code destinationTable}, each fully loaded with files (including their fields)
     * and webhooks.
     */
    List<DataSource> findAllWithDestinationTable();

    /**
     * Returns all data sources, fully loaded with files (including fields) and webhooks.
     */
    List<DataSource> findAll();

    /**
     * Returns all data sources matching the given JPA specification.
     */
    List<DataSource> findAll(Specification<DataSource> spec);

    /**
     * Retrieves a single data source by ID, fully loading files (with fields)
     * and webhooks.
     */
    Optional<DataSource> findById(Long id);

    /**
     * Persists a new or updated data source and publishes a
     * {@link io.github.eendroroy.loyalty.event.DataSourceSavedEvent} after commit.
     */
    DataSource save(DataSource entity);

    /**
     * Deletes the data source with the given ID and publishes a
     * {@link io.github.eendroroy.loyalty.event.DataSourceDeletedEvent} after commit.
     */
    void deleteById(Long id);

    /**
     * Atomically updates scalars and — when the corresponding argument is non-null —
     * replaces schema fields, files (with their fields), and/or webhooks.
     * Null arguments leave the existing sub-collection untouched.
     *
     * @param id            primary key of the data source to update
     * @param scalarUpdater consumer that applies changes to the managed entity
     * @param newFiles      replacement file list (each may include nested fields),
     *                      or {@code null} to leave files unchanged
     * @param newWebhooks   replacement webhook list, or {@code null} to leave webhooks unchanged
     * @param newSchemaFields replacement schema field list, or {@code null} to leave schema fields unchanged
     * @return the updated entity wrapped in an {@link Optional}, or empty if not found
     */
    Optional<DataSource> update(Long id, Consumer<DataSource> scalarUpdater,
                                List<DataSourceFile> newFiles,
                                List<DataSourceWebhook> newWebhooks,
                                List<DataSourceSchemaField> newSchemaFields);

    List<DataSource> findArchived();

    void archiveById(Long id);

    void purgeArchivedById(Long id);
}
