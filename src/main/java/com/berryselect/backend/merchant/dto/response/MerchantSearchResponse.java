package com.berryselect.backend.merchant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSearchResponse {
    private List<MerchantInfo> merchants;
    private Boolean hasMore; // 더 가져올 데이터가 있는지
    private Long lastId; // 다음 요청 시 사용할 마지막 ID

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MerchantInfo {
        private Long id;
        private String name;
        private Long brandId;
        private String brandName;
        private Long categoryId;
        private String categoryName;
        private String address;
        private BigDecimal lat;
        private BigDecimal lng;
        private String kakaoPlaceId;

        // 거리 정보
        private Double distanceMeters;   // 거리 (미터, 정렬용)
        private String distanceText;     // 거리 텍스트 ("120m", "1.2km")
    }
}