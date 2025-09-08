package com.berryselect.backend.benefits.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "benefit_rule_limits")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class BenefitRuleLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private BenefitRule rule;

    @Column(nullable = false, length = 20)
    private String limitType; // MONTHLY / DAILY / PER_TX

    private Integer limitAmount;
    private Integer limitCount;
}
