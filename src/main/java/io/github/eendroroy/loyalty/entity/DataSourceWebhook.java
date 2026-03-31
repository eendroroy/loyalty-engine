package io.github.eendroroy.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Auto-managed webhook endpoint for a {@link DataSource}.
 * Exactly one webhook exists per data source (created automatically on source creation).
 *
 * <p>The webhook path is derived from the parent data source name:
 * {@code /web-hook?dataSourceName={dataSourceName}}. Properties are identical to the parent
 * data source's {@link DataSourceSchemaField} list — no separate mapping needed.
 */
@Getter
@Setter
@Entity
@Table(name = "data_source_webhook")
@EntityListeners(AuditingEntityListener.class)
public class DataSourceWebhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The data source that owns this webhook. Unique — one webhook per data source. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id", nullable = false, unique = true)
    private DataSource dataSource;

    /** Optional description of what this webhook is used for. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Properties that define the expected JSON payload structure. */
    @JsonIgnoreProperties("webhook")
    @OneToMany(mappedBy = "webhook", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DataSourceWebhookProperty> properties = new ArrayList<>();

    /**
     * Whether this webhook is active. When {@code false} the ingestion controller
     * rejects incoming payloads with {@code 503 Service Unavailable}.
     * Defaults to {@code false} (disabled) on creation.
     */
    @Column(name = "enabled", nullable = false, columnDefinition = "boolean NOT NULL DEFAULT false")
    private boolean enabled = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Generates the webhook endpoint path from the parent data source's name.
     * Returns a path like {@code /web-hook?dataSourceName={dataSourceName}}.
     */
    @Transient
    public String generateEndpoint() {
        if (dataSource == null || dataSource.getName() == null) {
            return null;
        }
        return "/web-hook?dataSourceName=" + dataSource.getName();
    }
}
