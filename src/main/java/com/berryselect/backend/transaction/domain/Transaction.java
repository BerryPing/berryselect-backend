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
    private Long txId;

    @Column(nullable = false)
    private Long userId;

    private Long merchantId;
    private Long categoryId;
    private Integer paidAmount;

    @Column(nullable = false, updatable = false)
    private Instant txTime;

    private Long optionId;

    @Column(name = "session_id")
    private Long sessionId;


    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (txTime == null) txTime = now;
        if (createdAt == null) createdAt = now;
    }
}