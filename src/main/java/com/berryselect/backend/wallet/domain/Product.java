package com.berryselect.backend.wallet.domain;

import com.berryselect.backend.wallet.domain.type.AssetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

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

    @Column(length = 255)
    private String brand;

    @Column(name = "face_value")
    private Integer faceValue;

    @Column(name = "barcode_type", length = 20)
    private String barcodeType;

    @Column(name = "level_schema", columnDefinition = "JSON")
    private String levelSchema;

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