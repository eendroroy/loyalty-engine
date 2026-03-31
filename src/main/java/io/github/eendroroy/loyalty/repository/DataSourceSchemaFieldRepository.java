package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.DataSourceSchemaField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DataSourceSchemaFieldRepository extends JpaRepository<DataSourceSchemaField, Long> {

    /** Returns all schema fields for the given data source. */
    List<DataSourceSchemaField> findByDataSourceId(Long dataSourceId);

    /** Returns all schema fields for the given data source, ordered by ID. */
    List<DataSourceSchemaField> findByDataSourceIdOrderById(Long dataSourceId);

    /**
     * Bulk-delete all schema fields for a data source.
     * Must be called before replacing the field list to avoid unique-constraint violations.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DataSourceSchemaField f WHERE f.dataSource.id = :dataSourceId")
    void deleteByDataSourceId(@Param("dataSourceId") Long dataSourceId);
}

