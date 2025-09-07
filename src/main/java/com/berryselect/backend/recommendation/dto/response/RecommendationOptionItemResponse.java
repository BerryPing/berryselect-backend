package com.berryselect.backend.recommendation.dto.response;


import com.berryselect.backend.recommendation.domain.RecommendationOptionItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendationOptionItemResponse {
    private Long itemId;
    private String componentType;
    private Long componentRefId;
    private Long ruleId;        // 필요하다면 ruleId도 내려줌
    private String title;
    private String subtitle;
    private Integer appliedValue;
    private Short sortOrder;

    public static RecommendationOptionItemResponse fromEntity(RecommendationOptionItem item) {
        return RecommendationOptionItemResponse.builder()
                .itemId(item.getItemId())
                .componentType(item.getComponentType())
                .componentRefId(item.getComponentRefId())
                .ruleId(item.getRuleId()) // Domain에 있으면 그대로 매핑
                .title(item.getTitle())
                .subtitle(item.getSubtitle())
                .appliedValue(item.getAppliedValue())
                .sortOrder(item.getSortOrder())
                .build();
    }
}

