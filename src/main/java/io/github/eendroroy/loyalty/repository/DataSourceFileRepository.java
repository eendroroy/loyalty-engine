package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.DataSourceFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DataSourceFileRepository extends JpaRepository<DataSourceFile, Long> {

    List<DataSourceFile> findByDataSourceId(Long dataSourceId);

    /** Load all watched-file configs with their parent source and field definitions. */
    @Query("SELECT DISTINCT f FROM DataSourceFile f "
           + "JOIN FETCH f.dataSource ds "
           + "LEFT JOIN FETCH f.fields")
    List<DataSourceFile> findAllWithDataSourceAndFields();

    /** Load a single watched-file config with its parent source and field definitions. */
    @Query("SELECT f FROM DataSourceFile f "
           + "JOIN FETCH f.dataSource ds "
           + "LEFT JOIN FETCH f.fields "
           + "WHERE f.id = :id")
    Optional<DataSourceFile> findByIdWithDataSourceAndFields(@Param("id") Long id);

    /** Load all files for a data source with their field definitions. */
    @Query("SELECT DISTINCT f FROM DataSourceFile f "
           + "LEFT JOIN FETCH f.fields "
           + "WHERE f.dataSource.id = :dataSourceId")
    List<DataSourceFile> findByDataSourceIdWithFields(@Param("dataSourceId") Long dataSourceId);

    /** Bulk-delete all file entries for a data source (same ordering guarantee as field deletion). */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DataSourceFile f WHERE f.dataSource.id = :dataSourceId")
    void deleteByDataSourceId(@Param("dataSourceId") Long dataSourceId);
}
