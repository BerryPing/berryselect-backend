package com.berryselect.backend.transaction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id")
    private Long txId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "mcc")
    private Integer mcc;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "KRW";

    @Column(name = "payment_method", length = 40)
    private String paymentMethod;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "payment_asset_id")
    private Long paymentAssetId;

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    @Column(name = "tx_time", nullable = false, columnDefinition = "DATETIME(3)")
    private LocalDateTime txTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(3)" )
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "txId", fetch = FetchType.LAZY)
    @Builder.Default
    private List<AppliedBenefit> appliedBenefits = new ArrayList<>();
}
