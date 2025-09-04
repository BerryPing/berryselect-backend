package com.berryselect.backend.transaction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "applied_benefits")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppliedBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tx_id", nullable = false)
    private Long txId;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "source_ref")
    private Long sourceRef;

    @Column(name = "saved_amount", nullable = false)
    private Integer savedAmount;
}
