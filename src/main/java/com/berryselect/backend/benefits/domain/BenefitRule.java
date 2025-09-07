package com.berryselect.backend.benefits.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "benefit_rules")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class BenefitRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    @Column(nullable = false, length = 20)
    private String benefitType; // DISCOUNT / CASHBACK / POINT

    @Column(nullable = false, length = 20)
    private String valueType;   // RATE / AMOUNT

    private BigDecimal valueRate;   // RATE일 때
    private Integer valueAmount;    // AMOUNT일 때

    private Integer minAmount;
    private Integer maxBenefitAmount;

    private String description;
    private Short priority;
    private Boolean isActive;

    private LocalDate validFrom;
    private LocalDate validTo;

    @Column(name = "source_type", length = 20)
    private String sourceType;  // CARD / MEMBERSHIP / GIFTICON

    @Column(name = "source_ref_id")
    private Long sourceRefId;   // FK → products.id

    private Boolean isExclusive;

    private Instant createdAt;
    private Instant updatedAt;

    // ✅ 관계 매핑
    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BenefitRuleScope> scopes;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BenefitRuleLimit> limits;
}
