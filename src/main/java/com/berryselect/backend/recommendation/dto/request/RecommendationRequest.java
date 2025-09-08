package com.berryselect.backend.recommendation.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RecommendationRequest {
    private Integer amount;
    private Boolean useGifticon;
    private Long merchantId;
}