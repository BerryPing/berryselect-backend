package com.berryselect.backend.recommendation.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Entity
@Table(name = "recommendation_option_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationOptionItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private RecommendationOption option;

    private String componentType; // CARD / MEMBERSHIP / GIFTICON
    private Long componentRefId;
    private Long ruleId;
    private String title;
    private String subtitle;
    private Integer appliedValue;
    private Short sortOrder;
}
