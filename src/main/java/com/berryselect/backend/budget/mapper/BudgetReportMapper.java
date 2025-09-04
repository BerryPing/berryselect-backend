package com.berryselect.backend.budget.mapper;

import com.berryselect.backend.budget.dto.response.CategorySpendingResponse;
import com.berryselect.backend.budget.dto.response.MonthlyReportDetailResponse;
import com.berryselect.backend.budget.dto.response.RecommendationUsageResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Budget 도메인 → DTO 단순 변환 매퍼
 * - 순수한 필드 매핑만 담당
 * - 비즈니스 로직은 Service에서 처리
 */
@Component
public class BudgetReportMapper {

    /**
     * 월별 리포트 상세 응답 생성 (모든 계산된 값들을 Service에서 받아서 조합)
     * @param yearMonth 조회 년월
     * @param totalSpent 총 지출액 (Service에서 계산)
     * @param totalSaved 총 절약액 (Service에서 계산)
     * @param totalTransactionCount 총 거래건수 (Service에서 계산)
     * @param categorySpending 카테고리별 지출 목록 (Service에서 생성)
     * @param recommendationUsage 추천 사용률 (Service에서 생성)
     * @param aiSummary AI 요약 내용
     * @param savingRate 절약률 (Service에서 계산)
     * @return 월별 상세 리포트 응답 DTO
     */
    public MonthlyReportDetailResponse toMonthlyReportDetailResponse(
            String yearMonth,
            Long totalSpent,
            Long totalSaved,
            Integer totalTransactionCount,
            List<CategorySpendingResponse> categorySpending,
            RecommendationUsageResponse recommendationUsage,
            String aiSummary,
            Double savingRate) {

        return MonthlyReportDetailResponse.builder()
                .yearMonth(yearMonth)
                .totalSpent(totalSpent != null ? totalSpent : 0L)
                .totalSaved(totalSaved != null ? totalSaved : 0L)
                .totalTransactionCount(totalTransactionCount != null ? totalTransactionCount : 0)
                .categorySpending(categorySpending != null ? categorySpending : List.of())
                .recommendationUsage(recommendationUsage)
                .aiSummary(aiSummary)
                .build();
    }

    /**
     * 카테고리별 지출 응답 생성
     * @param categoryId 카테고리 ID
     * @param categoryName 카테고리명 (Service에서 조회)
     * @param amountSpent 지출액
     * @param transactionCount 거래건수
     * @param spendingRatio 지출 비율 (Service에서 계산)
     * @param savingRate 절약률 (Service에서 계산)
     * @param chartColor 차트 색상 (Service에서 할당)
     * @return 카테고리별 지출 응답 DTO
     */
    public CategorySpendingResponse toCategorySpendingResponse(
            Long categoryId,
            String categoryName,
            Long amountSpent,
            Integer transactionCount,
            Double spendingRatio,
            Double savingRate,
            String chartColor) {

        return CategorySpendingResponse.builder()
                .categoryId(categoryId)
                .categoryName(categoryName != null ? categoryName : "기타")
                .amountSpent(amountSpent != null ? amountSpent : 0L)
                .transactionCount(transactionCount != null ? transactionCount : 0)
                .spendingRatio(spendingRatio != null ? spendingRatio : 0.0)
                .categorySavingRate(savingRate != null ? savingRate : 0.0)
                .chartColor(chartColor != null ? chartColor : "#6366F1")
                .build();
    }

    /**
     * 추천 사용률 통계 응답 생성
     * @param totalTransactions 전체 거래 수
     * @param recommendationUsedTransactions 추천 사용 거래 수
     * @param usageRate 사용률 (Service에서 계산)
     * @param totalSavedFromRecommendation 추천을 통한 총 절약액
     * @param averageSaving 평균 절약액 (Service에서 계산)
     * @return 추천 사용률 응답 DTO
     */
    public RecommendationUsageResponse toRecommendationUsageResponse(
            Long totalTransactions,
            Long recommendationUsedTransactions,
            BigDecimal usageRate,
            Long totalSavedFromRecommendation,
            Long averageSaving) {

        return RecommendationUsageResponse.builder()
                .totalTransactions(totalTransactions != null ? totalTransactions : 0L)
                .recommendationUsedTransactions(recommendationUsedTransactions != null ? recommendationUsedTransactions : 0L)
                .usageRate(usageRate != null ? usageRate : BigDecimal.ZERO)
                .totalSavedFromRecommendation(totalSavedFromRecommendation != null ? totalSavedFromRecommendation : 0L)
                .averageSavingPerRecommendation(averageSaving != null ? averageSaving : 0L)
                .build();
    }

}
