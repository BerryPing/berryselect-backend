package com.berryselect.backend.budget.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 월별 상세 리포트 응답 DTO
 * - 프론트: 요약 페이지 전체 데이터
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReportDetailResponse {
    private String yearMonth; // 조회 년월
    private Long totalSpent; // 총 지출 금액
    private Long totalSaved; // 총 절약 금액
    private Integer totalTransactionCount; // 총 거래 건수
    @Builder.Default
    private List<CategorySpendingResponse> categorySpending = List.of(); // 카레고리별 지출 차트 데이터
    private RecommendationUsageResponse recommendationUsage; // 추천 시스템 사용률 통계
    private String aiSummary; // AI 분석 리포트 내용
    private Double savingRate; // 절약률
    private Long spentChangeFromLastMonth; // 전월 대비 지출 변화량 -> 시간 되면 확장
    private Long savedChangeFromLastMonth; // 전월 대비 절약 변화량 -> 시간 되면 확장
}
