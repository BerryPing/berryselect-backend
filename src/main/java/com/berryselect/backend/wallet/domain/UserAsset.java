package com.berryselect.backend.wallet.domain;

import com.berryselect.backend.wallet.domain.type.AssetType;
import com.berryselect.backend.wallet.domain.type.GifticonStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_assets")
@Getter
@Setter
public class UserAsset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;  // CARD/MEMBERSHIP/GIFTICON

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GifticonStatus gifticonStatus;  // ACTIVE/USED/EXPIRED

    @Column(length = 4)
    private String last4;

    @Column(name = "prev_month_spend")
    private Long prevMonthSpend;

    @Column(name = "this_month_spend")
    private Long thisMonthSpend;

    @Column(name = "external_no")
    private String externalNo;

    private Integer balance;
    private String barcode;
    private String level;
    private Integer priority;

    @Column(name = "limit_expected")
    private Integer limitExpected;

    @Column(name = "membership_point_balance")
    private Long membershipPointBalance;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}

