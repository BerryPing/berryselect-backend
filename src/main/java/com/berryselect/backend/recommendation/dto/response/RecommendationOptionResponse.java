package com.berryselect.backend.recommendation.dto.response;

import com.berryselect.backend.recommendation.domain.RecommendationOption;
import lombok.Getter;
import lombok.Builder;

import java.util.List;

@Getter
@Builder
public class RecommendationOptionResponse {
    private Long optionId;
    private Integer expectedPay;
    private Integer expectedSave;
    private Short rankOrder;
    private List<RecommendationOptionItemResponse> items;

    public static RecommendationOptionResponse fromEntity(RecommendationOption option) {
        return RecommendationOptionResponse.builder()
                .optionId(option.getOptionId())
                .expectedPay(option.getExpectedPay())
                .expectedSave(option.getExpectedSave())
                .rankOrder(option.getRankOrder())
                .items(option.getItems().stream()
                        .map(RecommendationOptionItemResponse::fromEntity)
                        .toList())
                .build();
    }
}
