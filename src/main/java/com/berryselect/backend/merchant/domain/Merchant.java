package com.berryselect.backend.merchant.domain;

import com.berryselect.backend.wallet.domain.Brand;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    // Category 엔티티와의 관계 (Lazy Loading)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    private Double lat;

    private Double lng;

    @Column(length = 255)
    private String address;

    @Column(name = "kakao_place_id", length = 64, unique = true)
    private String kakaoPlaceId;

    @Column(name = "created_at", updatable = false, insertable = false,
            columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)")
    private LocalDateTime updatedAt;

}

