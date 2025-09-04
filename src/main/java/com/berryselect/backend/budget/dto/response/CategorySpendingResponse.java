package com.berryselect.backend.budget.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리별 지출 정보 응답 DTO
 * - 프론트: 카테고리별 지출 차트/목록
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySpendingResponse {
    private Long categoryId; // 카테고리 ID
    private String categoryName; // 카레고리명
    private Long amountSpent; // 해당 카테고리 지출 금액
    private Integer transactionCount; // 해당 카테고리 거래 건수
    private Double spendingRatio; // 전체 지출 대비 비율 (%)
    private Double categorySavingRate; // 카테고리별 절약률 (%)
    private String chartColor; // 차트 표시용 색상 -> 프론트에서 사용
}
