package io.github.eendroroy.loyalty.entity;

import io.github.eendroroy.loyalty.enums.FieldDataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * JPA entity representing a single typed property expected in a webhook payload.
 *
 * <p>Analogous to {@link DataSourceField} for file-based sources. The {@link #name}
 * field is the JSON key in the incoming payload; the {@link #fieldAlias} is the
 * globally-unique token used in rule expressions and as the destination-table column name.
 *
 * <p>{@link #fieldAlias} must conform to the pattern {@code ^[a-zA-Z][a-zA-Z0-9._]*$}
 * and must be globally unique across all {@code DataSourceField} and
 * {@code DataSourceWebhookProperty} records.
 */
@Getter
@Setter
@Entity
@Table(
        name = "data_source_webhook_property",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_webhook_prop_alias",
                columnNames = {"webhook_id", "field_alias"}
        )
)
public class DataSourceWebhookProperty {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The webhook configuration that owns this property definition. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_id", nullable = false)
    private DataSourceWebhook webhook;

    /** JSON key in the incoming payload (e.g. {@code "transactionAmount"}). */
    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Destination column name / rule-expression token. Must equal a
     * {@link DataSourceSchemaField#getName()} value owned by the parent data source.
     * Pattern: {@code ^[a-zA-Z][a-zA-Z0-9._]*$}.
     *
     * <p><strong>Note:</strong> the per-column {@code UNIQUE} index from earlier versions
     * must be dropped manually if upgrading:
     * {@code ALTER TABLE data_source_webhook_property DROP CONSTRAINT IF EXISTS
     * data_source_webhook_property_field_alias_key;}
     */
    @NotBlank
    @Column(name = "field_alias", nullable = false)
    private String fieldAlias;

    /** Declared data type of this property's values. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private FieldDataType dataType;

    /**
     * Date/time format pattern (e.g. {@code yyyy-MM-dd}) used when
     * {@link #dataType} is {@code DATE}. Null/blank defaults to ISO-8601.
     */
    @Column(name = "format", length = 50)
    private String format;

    /** Human-readable description shown as a hint in the rule-expression autocomplete UI. */
    @Column(columnDefinition = "TEXT")
    private String description;
}
