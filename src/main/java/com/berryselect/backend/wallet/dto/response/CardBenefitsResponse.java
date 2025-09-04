package com.berryselect.backend.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardBenefitsResponse {

    private List<BenefitGroup> personalized; // 사용자가 선택한 3개 카테고리
    private List<BenefitGroup> others;       // 그 외 카테고리

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitGroup {
        private String category;
        private List<BenefitItem> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitItem {
        private String brand;
        private String title;
        private String subtitle;
    }
}