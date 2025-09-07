package com.berryselect.backend.transaction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Entity
@Table(name = "applied_benefits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppliedBenefit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tx_id")
    private Transaction tx;

    private Long ruleId;
    private String sourceType;   // CARD / MEMBERSHIP / GIFTICON
    private Long sourceRef;      // user_assets.id
    private Integer savedAmount;
}
