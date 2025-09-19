package com.berryselect.backend.merchant.domain;

import com.berryselect.backend.wallet.domain.Brand;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_cache")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MerchantCache {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_place_id", length = 64, nullable = false)
    private String providerPlaceId;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    private Double lat;

    private Double lng;

    @Column(length = 255)
    private String address;

    @Column(name = "created_at", updatable = false, insertable = false,
            columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime createdAt;

}