package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.DataSourceWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DataSourceWebhookRepository extends JpaRepository<DataSourceWebhook, Long> {

    List<DataSourceWebhook> findAllByDataSourceId(Long dataSourceId);

    /** Find the webhook for a data source (exactly one per source). */
    Optional<DataSourceWebhook> findByDataSourceId(Long dataSourceId);

    /** Load all webhooks joined with their parent data source. */
    @Query("SELECT w FROM DataSourceWebhook w JOIN FETCH w.dataSource")
    List<DataSourceWebhook> findAllWithDataSource();

    /**
     * Load all webhooks for a data source with their properties initialised.
     * Used as pass 3 in the multi-pass fetch pattern to avoid LazyInitializationException.
     */
    @Query("SELECT DISTINCT w FROM DataSourceWebhook w "
            + "LEFT JOIN FETCH w.properties WHERE w.dataSource.id = :dataSourceId")
    List<DataSourceWebhook> findAllWithPropertiesByDataSourceId(@Param("dataSourceId") Long dataSourceId);

    /** Load a single webhook with its properties and parent data source. */
    @Query("SELECT w FROM DataSourceWebhook w "
            + "JOIN FETCH w.dataSource LEFT JOIN FETCH w.properties WHERE w.id = :id")
    Optional<DataSourceWebhook> findByIdWithDataSourceAndProperties(@Param("id") Long id);

    /** Load a single webhook with its properties (no data source join). */
    @Query("SELECT w FROM DataSourceWebhook w LEFT JOIN FETCH w.properties WHERE w.id = :id")
    Optional<DataSourceWebhook> findByIdWithProperties(@Param("id") Long id);

    /** Find a webhook by endpoint path with its data source and properties loaded. */
    @Query("SELECT w FROM DataSourceWebhook w "
            + "JOIN FETCH w.dataSource ds LEFT JOIN FETCH w.properties "
            + "WHERE CONCAT('/web-hook?dataSourceName=', ds.name) = :endpoint")
    Optional<DataSourceWebhook> findByEndpointWithDataSourceAndProperties(@Param("endpoint") String endpoint);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DataSourceWebhook w WHERE w.dataSource.id = :dataSourceId")
    void deleteByDataSourceId(@Param("dataSourceId") Long dataSourceId);
}
