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
 * JPA entity representing a logical data ingestion source.
 *
 * <p>A single {@code DataSource} aggregates one or more {@link DataSourceFile} records
 * (each watched independently by the file-watcher service) and an optional
 * {@link DataSourceWebhook} endpoint for real-time push ingestion.
 *
 * <p>Field definitions now live on individual {@link DataSourceFile} entries,
 * allowing each file to have a different column layout. Use {@link #getAllFields()}
 * to aggregate fields across all files.
 */
@Getter
@Setter
@Entity
@Table(name = "data_source")
@EntityListeners(AuditingEntityListener.class)
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable name for this source — unique system-wide. */
    @NotBlank
    @Column(unique = true, nullable = false)
    private String name;

    /** Optional description of what this source represents. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Canonical destination-column schema for this data source.
     * These fields define the DDL for the destination table and serve as
     * rule-expression tokens.
     */
    @JsonIgnoreProperties("dataSource")
    @OneToMany(mappedBy = "dataSource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DataSourceSchemaField> schemaFields = new ArrayList<>();

    /** File paths registered for this source; each is watched independently. */
    @JsonIgnoreProperties("dataSource")
    @OneToMany(mappedBy = "dataSource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DataSourceFile> files = new ArrayList<>();

    /** Optional webhook endpoints for real-time push ingestion (zero or more). */
    @JsonIgnoreProperties("dataSource")
    @OneToOne(mappedBy = "dataSource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DataSourceWebhook webhook;

    /**
     * Name of the destination table — unique system-wide.
     * Must conform to the database naming conventions validated by DataSourceTableService.
     */
    @Column(name = "destination_table", unique = true)
    private String destinationTable;

    /** Timestamp when this record was first created; set by JPA auditing. */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the most recent update; maintained by JPA auditing. */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Indicates if this data source is archived (soft-deleted).
     */
    @Column(name = "archived", nullable = false, columnDefinition = "boolean NOT NULL DEFAULT false")
    private boolean archived = false;

    /**
     * Aggregates field definitions from <em>all</em> files belonging to this data source.
     * Used as a fallback when schema fields are not yet defined.
     */
    @Transient
    public List<DataSourceField> getAllFields() {
        if (files == null) return List.of();
        return files.stream()
                .filter(f -> f.getFields() != null)
                .flatMap(f -> f.getFields().stream())
                .toList();
    }
}
