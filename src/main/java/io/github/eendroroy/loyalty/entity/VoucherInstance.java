package io.github.eendroroy.loyalty.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Represents a single awarded instance of a {@link Voucher}.
 *
 * <p>Each instance carries a unique 7-character alphanumeric secret code
 * (all uppercase, system-wide unique) that is generated at award time
 * and presented to the customer for redemption.
 */
@Getter
@Setter
@Entity
@Table(name = "voucher_instance")
@EntityListeners(AuditingEntityListener.class)
public class VoucherInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The voucher template this instance belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    /** Unique 7-character alphanumeric (A-Z 0-9) secret code for the customer. */
    @NotBlank
    @Column(unique = true, nullable = false, length = 7)
    private String secretCode;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

