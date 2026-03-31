package io.github.eendroroy.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.eendroroy.loyalty.enums.FieldDataType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing a single typed column / field within a {@link DataSourceFile}.
 *
 * <p>The {@link #fieldAlias} is the token used in rule expressions
 * (e.g. {@code transaction.amount}) and must be globally unique across all data sources.
 * It must conform to the pattern {@code ^[a-zA-Z][a-zA-Z0-9._]*$}.
 *
 * <p>{@link #columnNumber} identifies the 1-based column position in the CSV/flat-file.
 */
@Getter
@Setter
@Entity
@Table(
        name = "data_source_field",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_field_file_alias",
                columnNames = {"data_source_file_id", "field_alias"}
        )
)
public class DataSourceField {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The parent file that owns this field definition. */
    @JsonIgnoreProperties("fields")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_file_id", nullable = false)
    private DataSourceFile dataSourceFile;

    /** Column header or raw field key as it appears in the source (e.g. "Amount"). */
    @NotBlank
    @Column(name = "field_name", nullable = false)
    private String fieldName;

    /**
     * Globally-unique alias used as a token in rule expressions and as the
     * destination-table column name. Must equal a {@link DataSourceSchemaField#getName()}
     * value owned by the same data source. Pattern: {@code ^[a-zA-Z][a-zA-Z0-9._]*$}.
     *
     * <p><strong>Note:</strong> the per-column {@code UNIQUE} index that existed in earlier
     * versions of this schema must be dropped manually if upgrading:
     * {@code ALTER TABLE data_source_field DROP CONSTRAINT IF EXISTS
     * data_source_field_field_alias_key;}
     */
    @NotBlank
    @Column(name = "field_alias", nullable = false)
    private String fieldAlias;

    /**
     * 1-based column index within the source file.
     * Only relevant for CSV file ingestion sources; leave {@code null} to match by
     * {@link #fieldName} against the CSV header row instead.
     */
    private Integer columnNumber;

    /** Declared data type of this field's values (STRING, INTEGER, DECIMAL, DATE, BOOLEAN). */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private FieldDataType dataType;

    /** Human-readable description shown as a hint in the rule-expression autocomplete UI. */
    @Column(columnDefinition = "TEXT")
    private String description;
}
