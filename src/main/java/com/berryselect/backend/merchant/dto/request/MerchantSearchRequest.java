package com.berryselect.backend.merchant.dto.request;

import lombok.Data;

@Data
public class MerchantSearchRequest {
    private String keyword; // 가맹점명, 브랜드명 검색용
    private Long categoryId; // 카테고리 ID 필터
    private String sortBy = "name"; // name, distance, id

    // 무한 스크롤용
    private Long lastId; // 마지막으로 받은 가맹점 ID (cursor)
    private Integer limit = 20; // 한 번에 가져올 개수

    // 거리 필터링 (km 단위)
    private Double maxDistanceKm; // 최대 거리 (예: 1.0 = 1km 이내)
}
