package io.github.eendroroy.loyalty.entity;

import io.github.eendroroy.loyalty.enums.FieldDataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a canonical destination-schema field on a {@link DataSource}.
 *
 * <p>A {@code DataSourceSchemaField} defines a single column in the destination table:
 * <ul>
 *   <li>{@link #name} is the column name used in the destination DDL and as the
 *       rule-expression token for this data source.</li>
 *   <li>{@link #dataType} is the <em>target</em> type (used for the DDL {@code CREATE TABLE}).</li>
 * </ul>
 *
 * <p>File-level {@link DataSourceField} entries reference a schema field via {@code fieldAlias},
 * which must equal a {@code DataSourceSchemaField.name} owned by the same data source.
 * {@link DataSourceWebhookProperty} entries work the same way.
 *
 * <p>{@link #name} is unique within each {@code DataSource} (not globally).
 */
@Getter
@Setter
@Entity
@Table(
        name = "data_source_schema_field",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_schema_field_ds_name",
                columnNames = {"data_source_id", "name"}
        )
)
@EntityListeners(AuditingEntityListener.class)
public class DataSourceSchemaField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The data source that owns this schema field. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    /**
     * Destination column name and rule-expression token for this data source.
     * Unique within the data source. Must match {@code ^[a-zA-Z][a-zA-Z0-9._]*$}.
     */
    @NotBlank
    @Column(nullable = false)
    private String name;

    /** Optional description shown as a tooltip/hint in the rule-expression autocomplete UI. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Target data type for the destination table column.
     * Used by {@link io.github.eendroroy.loyalty.service.DataSourceTableService}
     * to generate the correct SQL column type in {@code CREATE TABLE}.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private FieldDataType dataType;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

