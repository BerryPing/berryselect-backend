package com.berryselect.backend.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PersonalizedBenefitsResponse {

    /** 선호 카테고리 블록(피그마의 "맞춤형 혜택" 섹션) */
    private List<CategoryBlock> categories;

    /** 그 외 혜택(선호에 속하지 않는 나머지) */
    private List<BenefitCard> others;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CategoryBlock {
        private String categoryName;      // 예: "카페", "편의점", "교통" (현재 settings가 문자열이므로 name 사용)
        private List<BenefitCard> items;  // 해당 카테고리 혜택 카드들
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BenefitCard {
        private Long assetId;             // 사용자 보유 결제수단 id (UserAsset.id)
        private String methodType;        // CARD | MEMBERSHIP | PAY (필요시)
        private String methodName;        // 예: "KB국민카드", "GS&POINT" 등
        private String brandName;         // 예: "GS25"
        private String title;             // 예: "편의점 10% 할인"
        private String summary;           // 예: "월 최대 1만원 / 실적 30만원"
        private String badge;             // 예: "쿠폰", "적립", "실적"
    }
}