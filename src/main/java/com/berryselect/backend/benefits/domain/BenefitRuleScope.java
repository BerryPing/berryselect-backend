package com.berryselect.backend.benefits.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "benefit_rule_scopes")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class BenefitRuleScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private BenefitRule rule;

    @Column(nullable = false, length = 20)
    private String scopeType; // CATEGORY / BRAND / MERCHANT / DAY_OF_WEEK / TIME

    private Long brandId;
    private Long merchantId;
    private Long categoryId;

    private Double lat;
    private Double lng;
    private Integer radiusM;

    @Column(name = "day_of_week")
    private String dayOfWeek; // "SAT,SUN"

    private Integer startMinute;
    private Integer endMinute;
}

