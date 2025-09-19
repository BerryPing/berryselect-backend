package com.berryselect.backend.benefits.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_benefit_counters",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "rule_id", "period_key"}))
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class UserBenefitCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private BenefitRule rule;

    @Column(name = "period_key", nullable = false, length = 20)
    private String periodKey; // e.g. 2025-09 or 2025-09-05

    @Column(name = "amount_used")
    private Integer amountUsed;

    @Column(name = "count_used")
    private Integer countUsed;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
