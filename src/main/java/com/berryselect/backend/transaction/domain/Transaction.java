package com.berryselect.backend.transaction.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "payment_asset_id")
    private Long paymentAssetId;

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    @Column(name = "tx_time", nullable = false, updatable = false)
    private Instant txTime;

    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (txTime == null) txTime = now;
        if (createdAt == null) createdAt = now;
    }
}
