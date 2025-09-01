package com.berryselect.backend.wallet.domain;

import com.berryselect.backend.wallet.domain.type.AssetType;
import com.berryselect.backend.wallet.domain.type.GifticonStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @Column(name = "gifticon_status")
    private GifticonStatus gifticonStatus;  // ACTIVE/USED/EXPIRED

    @Column(length = 4)
    private String last4;

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

    private LocalDate expiresAt;
}

