package io.github.eendroroy.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.eendroroy.loyalty.enums.VoucherType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a redeemable voucher definition.
 *
 * <p>A {@code Voucher} defines the template: its public code, type, and the
 * maximum number of secret-code instances that may be generated from it.
 * Individual instances are represented by {@link VoucherInstance}.
 */
@Getter
@Setter
@Entity
@Table(name = "voucher")
@EntityListeners(AuditingEntityListener.class)
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable name for this voucher. */
    @NotBlank
    @Column(nullable = false)
    private String name;

    /** Public voucher code used to identify the voucher template. */
    @NotBlank
    @Column(unique = true, nullable = false)
    private String code;

    /** Optional description of the voucher's purpose or terms. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Category of voucher — currently only DISCOUNT is supported. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType voucherType = VoucherType.DISCOUNT;

    /** Maximum number of secret-code instances that may be generated. */
    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer count;

    /** Whether this voucher is currently active and can be awarded. */
    @Column(nullable = false)
    private Boolean active = true;

    /** Soft-delete flag; use archive/purge lifecycle instead of direct delete. */
    @Column(nullable = false)
    private Boolean archived = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties("voucher")
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VoucherInstance> instances = new ArrayList<>();
}

