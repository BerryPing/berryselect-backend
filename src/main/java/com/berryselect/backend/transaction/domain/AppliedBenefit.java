package com.berryselect.backend.transaction.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applied_benefits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppliedBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: applied_benefits.tx_id -> transactions.tx_id (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tx_id", nullable = false)
    private Transaction tx;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    // CARD / MEMBERSHIP / GIFTICON
    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    // user_assets.id
    @Column(name = "source_ref")
    private Long sourceRef;

    @Column(name = "saved_amount", nullable = false)
    private Integer savedAmount;
}
