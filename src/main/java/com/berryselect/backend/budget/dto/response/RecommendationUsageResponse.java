package com.berryselect.backend.budget.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 베리픽 결제 추천 사용률 통계 응답 DTO
 * - 프론트: 요약 페이지 추천 사용률 섹션
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationUsageResponse {
    private Long totalTransactions; // 전체 거래 건수 (해당 월)
    private Long recommendationUsedTransactions; // 베리픽 추천을 통해 결제한 거래 건수
    private BigDecimal usageRate; // 추천 사용률 (0.0 ~ 100.0)
    private Long totalSavedFromRecommendation; // 베리픽 추천을 통해 절약한 총 금액
    private Long averageSavingPerRecommendation; // 추천 시스템 평균 절약액 (원/건)
}
