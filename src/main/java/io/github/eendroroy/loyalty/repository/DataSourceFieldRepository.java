package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.DataSourceField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DataSourceFieldRepository
        extends JpaRepository<DataSourceField, Long>, JpaSpecificationExecutor<DataSourceField> {

    List<DataSourceField> findByDataSourceFileId(Long dataSourceFileId);

    /**
     * Returns all fields belonging to any file of the given data source.
     */
    @Query("SELECT f FROM DataSourceField f WHERE f.dataSourceFile.dataSource.id = :dataSourceId")
    List<DataSourceField> findByDataSourceId(@Param("dataSourceId") Long dataSourceId);

    /**
     * Bulk-delete all fields for a data source file using JPQL DML.
     * JPQL @Modifying queries execute synchronously (not queued), so DELETEs
     * reach the DB before subsequent INSERTs — preventing unique-constraint
     * violations when the same field alias is reused across an update.
     * {@code clearAutomatically = true} evicts affected rows from the L1 cache.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DataSourceField f WHERE f.dataSourceFile.id = :dataSourceFileId")
    void deleteByDataSourceFileId(@Param("dataSourceFileId") Long dataSourceFileId);
}
