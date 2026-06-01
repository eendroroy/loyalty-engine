package io.github.eendroroy.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single file path that a {@link DataSource} should ingest.
 * One {@link DataSource} may own many {@code DataSourceFile} records,
 * each watched independently by {@link io.github.eendroroy.loyalty.watcher.FileWatcherService}.
 *
 * <p>Each file carries its own set of {@link DataSourceField} definitions, allowing
 * different files under the same data source to have different column layouts.
 */
@Getter
@Setter
@Entity
@Table(name = "data_source_file")
@EntityListeners(AuditingEntityListener.class)
public class DataSourceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    /** Absolute path to the file (or directory) to watch — unique system-wide. */
    @NotBlank
    @Column(name = "file_path", nullable = false, unique = true)
    private String filePath;

    /** Optional human-readable note about this file path. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** CSV field/column delimiter character (default `,`). */
    @Column(name = "field_separator", length = 10)
    private String fieldSeparator;

    /** CSV quote character used to wrap fields that contain the delimiter (default `"`). */
    @Column(name = "quote_character", length = 10)
    private String quoteCharacter;

    /** Line separator string (informational; auto-detected during parsing). */
    @Column(name = "line_separator", length = 10)
    private String lineSeparator;

    /** Number of non-header lines to skip at the beginning of the file (default 0). */
    @Column(name = "skip_first_n_lines")
    private Integer skipFirstNLines;

    /**
     * Optional absolute path to a directory where successfully imported files are archived.
     * After a successful ingestion the original file is moved into this directory and
     * renamed with a timestamp suffix to avoid overwriting earlier archives.
     * When {@code null} the file is left in place.
     */
    @Column(name = "archive_directory")
    private String archiveDirectory;

    /** Typed field definitions that describe the schema of this file's data. */
    @JsonIgnoreProperties("dataSourceFile")
    @OneToMany(mappedBy = "dataSourceFile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DataSourceField> fields = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
