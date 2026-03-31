package io.github.eendroroy.loyalty.entity;

import io.github.eendroroy.loyalty.enums.FileProcessingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tracks every attempt to process a specific version of a watched file.
 *
 * <h3>Distributed deduplication</h3>
 * A file "version" is identified by the tuple
 * ({@code data_source_file_id}, {@code file_path}, {@code last_modified_millis},
 * {@code file_size_bytes}).  The unique constraint on these four columns means only
 * one application instance can successfully insert a row for a given version.
 * Any subsequent attempt — from a competing instance or a duplicate event — receives
 * a constraint violation and skips processing.
 *
 * <ul>
 *   <li>{@link FileProcessingStatus#IN_PROGRESS} — claimed; processing underway.</li>
 *   <li>{@link FileProcessingStatus#COMPLETED} — successfully ingested; will not retry.</li>
 *   <li>{@link FileProcessingStatus#FAILED} — parse error; the unique constraint
 *       row is deleted so the next event triggers a fresh attempt.</li>
 * </ul>
 */
@Getter
@Setter
@Entity
@Table(
    name = "file_processing_log",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_file_version",
        columnNames = {"data_source_file_id", "file_path", "last_modified_millis", "file_size_bytes"}
    )
)
public class FileProcessingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_file_id", nullable = false)
    private DataSourceFile dataSourceFile;

    /** Full absolute path of the file that was processed. */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    /** File name only (for display). */
    @Column(name = "file_name")
    private String fileName;

    /** File size in bytes at the time of processing. */
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    /** {@code lastModified} epoch-milliseconds — part of the uniqueness key. */
    @Column(name = "last_modified_millis")
    private Long lastModifiedMillis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileProcessingStatus status;

    @Column(name = "rows_ingested")
    private Long rowsIngested;

    @Column(name = "rows_skipped")
    private Long rowsSkipped;

    private Long errors;

    /** JVM identity of the instance that processed this file (PID@hostname). */
    @Column(name = "instance_id")
    private String instanceId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}

