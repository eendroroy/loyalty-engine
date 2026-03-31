package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.DataSourceWebhookProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DataSourceWebhookPropertyRepository
        extends JpaRepository<DataSourceWebhookProperty, Long> {

    List<DataSourceWebhookProperty> findAllByWebhookId(Long webhookId);

    /**
     * Bulk-deletes all properties for a single webhook using JPQL DML.
     * Runs synchronously so DELETEs reach the DB before subsequent INSERTs —
     * preventing unique-constraint violations on {@code field_alias} when
     * the same alias is reused across an update.
     * {@code clearAutomatically = true} evicts affected rows from the L1 cache.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DataSourceWebhookProperty p WHERE p.webhook.id = :webhookId")
    void deleteByWebhookId(@Param("webhookId") Long webhookId);

    /**
     * Bulk-deletes all properties for a set of webhook IDs.
     * Must be called before bulk-deleting webhooks to avoid FK constraint violations.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DataSourceWebhookProperty p WHERE p.webhook.id IN :webhookIds")
    void deleteByWebhookIdIn(@Param("webhookIds") List<Long> webhookIds);
}

