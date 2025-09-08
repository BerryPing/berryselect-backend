package com.berryselect.backend.recommendation.dto.response;


import com.berryselect.backend.recommendation.domain.RecommendationSession;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
public class RecommendationResponse {
    private Long sessionId;
    private Integer inputAmount;
    private Boolean useGifticon;
    private List<RecommendationOptionResponse> options;

    public static RecommendationResponse fromEntity(RecommendationSession session) {
        return RecommendationResponse.builder()
                .sessionId(session.getSessionId())
                .inputAmount(session.getInputAmount())
                .useGifticon(session.getUseGifticon())
                .options(session.getOptions().stream()
                        .map(RecommendationOptionResponse::fromEntity)
                        .toList())
                .build();
    }
}
