package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.DataSourceWebhookProperty;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link DataSourceWebhookProperty} records.
 */
public interface DataSourceWebhookPropertyService {

    /** Returns all properties for the given webhook. */
    List<DataSourceWebhookProperty> findAllByWebhookId(Long webhookId);

    /** Looks up a single property by its primary key. */
    Optional<DataSourceWebhookProperty> findById(Long id);

    /** Persists (insert or update) a property record. */
    DataSourceWebhookProperty save(DataSourceWebhookProperty entity);

    /**
     * Bulk-deletes all properties for the given webhook.
     * Must be called before bulk-deleting the parent webhook to avoid FK violations.
     */
    void deleteByWebhookId(Long webhookId);

    /** Deletes a single property by its primary key. */
    void deleteById(Long id);
}

