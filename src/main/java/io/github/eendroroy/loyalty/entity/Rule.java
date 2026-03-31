package io.github.eendroroy.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.eendroroy.loyalty.enums.RuleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rule")
@EntityListeners(AuditingEntityListener.class)
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String ruleExpression; // e.g. WHEN transaction.amount > 20 AND DATE >= 2025-05-01 THEN 30 POINT

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleStatus status = RuleStatus.DRAFT;

    @Column(nullable = false)
    private Integer priority = 0; // higher value = higher priority

    private LocalDate activeFrom;
    private LocalDate activeTo;

    @Column(name = "frequency")
    private String frequency; // cron expression for scheduled rule evaluation

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties("rule")
    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RuleAction> actions = new ArrayList<>();
}
