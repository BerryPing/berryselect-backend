package com.berryselect.backend.wallet.domain;

import com.berryselect.backend.wallet.domain.type.AssetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private AssetType productType;  // CARD/MEMBERSHIP/GIFTICON

    @Column(length = 60)
    private String issuer;  // 카드사

    @Column(nullable = false, length = 255)
    private String name;
}