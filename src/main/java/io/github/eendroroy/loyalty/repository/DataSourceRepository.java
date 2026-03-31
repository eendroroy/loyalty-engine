package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DataSourceRepository
        extends JpaRepository<DataSource, Long>, JpaSpecificationExecutor<DataSource> {

    // ── Multi-pass fetch: all data sources ───────────────────────────────────

    /** Fetch pass 1 — initialises the {@code files} bag (fields live on files now). */
    @Query("SELECT DISTINCT ds FROM DataSource ds LEFT JOIN FETCH ds.files")
    List<DataSource> findAllWithFiles();

    /** Fetch pass 2 — initialises the {@code webhook}. */
    @Query("SELECT DISTINCT ds FROM DataSource ds LEFT JOIN FETCH ds.webhook")
    List<DataSource> findAllWithWebhooks();

    // ── Multi-pass fetch: single data source ─────────────────────────────────

    /** Fetch pass 1 — initialises the {@code files} bag for a single entity. */
    @Query("SELECT DISTINCT ds FROM DataSource ds LEFT JOIN FETCH ds.files WHERE ds.id = :id")
    Optional<DataSource> findByIdWithFiles(@Param("id") Long id);

    /** Fetch pass 2 — initialises the {@code webhook} for a single entity. */
    @Query("SELECT DISTINCT ds FROM DataSource ds LEFT JOIN FETCH ds.webhook WHERE ds.id = :id")
    Optional<DataSource> findByIdWithWebhooks(@Param("id") Long id);

    // ── Multi-pass fetch: schema fields ───────────────────────────────────────

    /** Fetch pass — initialises the {@code schemaFields} bag for all data sources. */
    @Query("SELECT DISTINCT ds FROM DataSource ds LEFT JOIN FETCH ds.schemaFields")
    List<DataSource> findAllWithSchemaFields();

    /** Fetch pass — initialises the {@code schemaFields} bag for a single entity. */
    @Query("SELECT DISTINCT ds FROM DataSource ds LEFT JOIN FETCH ds.schemaFields WHERE ds.id = :id")
    Optional<DataSource> findByIdWithSchemaFields(@Param("id") Long id);

    // ── Filtered: data sources that have a destination table ─────────────────

    @Query("SELECT DISTINCT ds FROM DataSource ds LEFT JOIN FETCH ds.files "
           + "WHERE ds.destinationTable IS NOT NULL AND ds.destinationTable <> ''")
    List<DataSource> findAllWithFilesAndTable();

    @Query("SELECT DISTINCT ds FROM DataSource ds LEFT JOIN FETCH ds.webhook "
           + "WHERE ds.destinationTable IS NOT NULL AND ds.destinationTable <> ''")
    List<DataSource> findAllWithWebhooksAndTable();

    List<DataSource> findByArchived(boolean archived);

    /**
     * Returns {@code true} when another non-archived source (different ID) already uses
     * the same destination table — used by the purge check to avoid a full table scan.
     */
    boolean existsByDestinationTableAndIdNotAndArchivedFalse(String destinationTable, Long id);
}
