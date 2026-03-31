package io.github.eendroroy.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.eendroroy.loyalty.enums.RewardType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rule_action")
public class RuleAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties("actions")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private RewardType rewardType;

    @NotBlank
    @Column(name = "reward_amount", nullable = false)
    private String rewardAmount;    // e.g. "30" for 30 points, "10.00" for money amount

    private String description;     // e.g. "30 point reward for May promotion"
}
