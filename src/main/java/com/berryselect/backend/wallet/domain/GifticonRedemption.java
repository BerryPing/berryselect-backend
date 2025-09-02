package com.berryselect.backend.wallet.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "gifticon_redemptions")
@Getter
@Setter
public class GifticonRedemption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private UserAsset asset;

    @Column(name = "tx_id")
    private Long txId;

    @Column(name = "used_amount", nullable = false)
    private Integer usedAmount;

    @Column(name = "redeemeed_at", nullable = false)
    private LocalDateTime redeemedAt;
}
