package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.FileProcessingLog;
import io.github.eendroroy.loyalty.enums.FileProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileProcessingLogRepository extends JpaRepository<FileProcessingLog, Long> {

    /** Returns the most recent log entry for a given file config, for monitoring display. */
    Optional<FileProcessingLog> findTopByDataSourceFileIdOrderByStartedAtDesc(Long dataSourceFileId);

    long countByDataSourceFileIdAndStatus(Long dataSourceFileId, FileProcessingStatus status);

    /** Check whether a specific file version has already been claimed or completed. */
    Optional<FileProcessingLog> findByDataSourceFileIdAndFilePathAndLastModifiedMillisAndFileSizeBytes(
            Long dataSourceFileId, String filePath, Long lastModifiedMillis, Long fileSizeBytes);

    /** Paginated log listing used by the monitoring endpoint. */
    @Query("SELECT l FROM FileProcessingLog l JOIN FETCH l.dataSourceFile f JOIN FETCH f.dataSource "
           + "ORDER BY l.startedAt DESC")
    Page<FileProcessingLog> findAllWithDetails(Pageable pageable);

    /** Delete a failed log so the file becomes eligible for retry. */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM FileProcessingLog l WHERE l.dataSourceFile.id = :fileId "
           + "AND l.filePath = :filePath AND l.lastModifiedMillis = :lastModified "
           + "AND l.fileSizeBytes = :fileSize AND l.status = 'FAILED'")
    void deleteFailedEntry(@Param("fileId") Long fileId, @Param("filePath") String filePath,
                           @Param("lastModified") Long lastModified, @Param("fileSize") Long fileSize);

    /** Remove all processing-log rows for a single file config (used before file deletion). */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM FileProcessingLog l WHERE l.dataSourceFile.id = :fileId")
    void deleteByDataSourceFileId(@Param("fileId") Long fileId);

    /** Remove all processing-log rows for a set of file configs (used before bulk file deletion). */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM FileProcessingLog l WHERE l.dataSourceFile.id IN :fileIds")
    void deleteByDataSourceFileIdIn(@Param("fileIds") List<Long> fileIds);
}

