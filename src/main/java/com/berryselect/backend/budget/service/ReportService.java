package com.berryselect.backend.budget.service;

import com.berryselect.backend.budget.domain.AnalysisReport;
import com.berryselect.backend.budget.domain.MonthlyCategorySummary;
import com.berryselect.backend.budget.domain.ReportType;
import com.berryselect.backend.budget.dto.response.CategorySpendingResponse;
import com.berryselect.backend.budget.dto.response.MonthlyReportDetailResponse;
import com.berryselect.backend.budget.dto.response.RecommendationUsageResponse;
import com.berryselect.backend.budget.mapper.BudgetReportMapper;
import com.berryselect.backend.budget.repository.AnalysisReportRepository;
import com.berryselect.backend.budget.repository.MonthlyCategorySummaryRepository;
import com.berryselect.backend.merchant.domain.Category;
import com.berryselect.backend.merchant.repository.CategoryRepository;
import com.berryselect.backend.transaction.repository.AppliedBenefitRepository;
import com.berryselect.backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 예산 및 리포트 관련 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final MonthlyCategorySummaryRepository monthlyCategorySummaryRepository;
    private final AnalysisReportRepository analysisReportRepository;
    private final TransactionRepository transactionRepository;
    private final AppliedBenefitRepository appliedBenefitRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetReportMapper budgetReportMapper;
    private final OpenAiService openAiService;

    /**
     * 월별 상세 리포트 조회
     */
    public MonthlyReportDetailResponse getMonthlyReportDetail(Long userId, String yearMonth) {
        log.info("월별 상세 리포트 조회 - userId: {}, yearMonth: {}", userId, yearMonth);

        // 1. 월별 요약 데이터 로드
        List<MonthlyCategorySummary> summaries =
                monthlyCategorySummaryRepository.findByUserIdAndYearMonthOrderByAmountSpentDesc(userId, yearMonth);

        // 2. 리스트에서 직접 총 지출/절약/건수를 계산
        Long totalSpent = summaries.stream()
                .mapToLong(MonthlyCategorySummary::getAmountSpent)
                .sum();
        Long totalSaved = summaries.stream()
                .mapToLong(MonthlyCategorySummary::getAmountSaved)
                .sum();
        Integer totalTransactionCount = summaries.stream()
                .mapToInt(MonthlyCategorySummary::getTxCount)
                .sum();

        // 3. 기타 계산 및 응답 DTO 생성
        List<CategorySpendingResponse> categorySpending =
                createCategorySpendingResponses(summaries, totalSpent);

        RecommendationUsageResponse recommendationUsage =
                createRecommendationUsageResponse(userId, yearMonth);

        String aiSummary = getOrGenerateAiSummary(userId, yearMonth, summaries);
        Double savingRate = calculateSavingRate(totalSaved, totalSpent);

        return budgetReportMapper.toMonthlyReportDetailResponse(
                yearMonth, totalSpent, totalSaved, totalTransactionCount,
                categorySpending, recommendationUsage, aiSummary, savingRate);
    }

    /**
     * AI 분석 리포트 생성 또는 조회
     */
    @Transactional
    public String generateOrGetAiSummary(Long userId, String yearMonth) {
        log.info("AI 분석 리포트 생성/조회 - userId: {}, yearMonth: {}", userId, yearMonth);

        String existingReport = getAiSummary(userId, yearMonth); // DB에 있는지 조회
        if (existingReport != null) {
            return existingReport;
        }

        List<MonthlyCategorySummary> summaries = monthlyCategorySummaryRepository
                .findByUserIdAndYearMonthOrderByAmountSpentDesc(userId, yearMonth);

        log.info("=== Repository 조회 파라미터 ===");
        log.info("조회할 userId: '{}' (타입: {})", userId, userId.getClass().getSimpleName());
        log.info("조회할 yearMonth: '{}' (타입: {})", yearMonth, yearMonth.getClass().getSimpleName());
        log.info("조회 결과 개수: {}", summaries.size());

        if (summaries.isEmpty()) {
            log.warn("Repository에서 데이터를 찾지 못함!");
        } else {
            log.info("첫 번째 데이터: userId={}, yearMonth={}, categoryId={}, amountSpent={}",
                    summaries.get(0).getUserId(),
                    summaries.get(0).getYearMonth(),
                    summaries.get(0).getCategoryId(),
                    summaries.get(0).getAmountSpent());
        }

        // 새로 AI 리포트 생성
        String aiContent = generateAiSummaryContent(yearMonth, summaries);
        if(aiContent == null) {
            return "AI 분석을 생성할 수 없습니다.";
        }

        // 성공시 DB에 INSERT
        AnalysisReport aiReport = AnalysisReport.builder()
                .userId(userId)
                .yearMonth(yearMonth)
                .reportType(ReportType.AI)
                .content(aiContent)
                .build();

        analysisReportRepository.save(aiReport);

        return aiContent;
    }

    // ======================== Private Helper Methods ========================

    private List<CategorySpendingResponse> createCategorySpendingResponses(
            List<MonthlyCategorySummary> summaries, Long totalSpent) {

        List<CategorySpendingResponse> responses = new ArrayList<>();

        for (MonthlyCategorySummary summary : summaries) {
            String categoryName = getCategoryName(summary.getCategoryId());
            Double spendingRatio = calculateSpendingRatio(summary.getAmountSpent(), totalSpent);
            Double categorySavingRate = calculateSavingRate(summary.getAmountSaved(), summary.getAmountSpent());

            CategorySpendingResponse response = budgetReportMapper.toCategorySpendingResponse(
                    summary.getCategoryId(), categoryName, summary.getAmountSpent(),
                    summary.getTxCount(), spendingRatio, categorySavingRate);

            responses.add(response);
        }

        return responses;
    }

    private RecommendationUsageResponse createRecommendationUsageResponse(Long userId, String yearMonth) {
        // KST 기준 범위 계산
        Instant[] range = getStartEndUtc(yearMonth);
        Instant startUtc = range[0];
        Instant endUtc   = range[1];

        // 변경된 Repository 메서드 호출
        Long totalTransactions = transactionRepository.countTotalTransactionsByMonth(userId, startUtc, endUtc);
        Long recommendationUsedTransactions = transactionRepository.countRecommendationUsedTransactions(userId, startUtc, endUtc);

        BigDecimal usageRate = calculateUsageRate(recommendationUsedTransactions, totalTransactions);
        Long totalSavedFromRecommendation = calculateSavedFromRecommendations(userId, startUtc, endUtc);
        Long averageSaving = calculateAverageSaving(totalSavedFromRecommendation, recommendationUsedTransactions);

        return budgetReportMapper.toRecommendationUsageResponse(
                totalTransactions, recommendationUsedTransactions, usageRate,
                totalSavedFromRecommendation, averageSaving);
    }

    private String getOrGenerateAiSummary(Long userId, String yearMonth, List<MonthlyCategorySummary> summaries) {
        String existingReport = getAiSummary(userId, yearMonth);
        if (existingReport != null) {
            return existingReport;
        }

        return generateAiSummaryContent(yearMonth, summaries);
    }

    // String "2024-12" → YearMonth 변환
    // ReportType.AI → "AI" 변환
    private String getAiSummary(Long userId, String yearMonth) {
        return analysisReportRepository
                .findByUserIdAndYearMonthAndReportType(userId, yearMonth, ReportType.AI)
                .stream()
                .findFirst()
                .map(AnalysisReport::getContent)
                .orElse(null);
    }

    /**
     * AI 분석 리포트 내용 생성 (OpenAI 사용)
     */
    private String generateAiSummaryContent(String yearMonth, List<MonthlyCategorySummary> summaries) {
        if (summaries.isEmpty()) {
            return String.format("%s에는 거래 내역이 없습니다.", yearMonth);
        }

        try {
            // OpenAI API 호출
            return openAiService.generateConsumptionAnalysis(yearMonth, summaries);
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    private Long calculateSavedFromRecommendations(Long userId, Instant startUtc, Instant endUtc) {
        Long totalSaved = appliedBenefitRepository.getTotalSavedByUserAndMonth(userId, startUtc, endUtc);
        Double usageRate = getRecommendationUsageRate(userId, startUtc, endUtc);
        return Math.round(totalSaved * usageRate);
    }

    private Double getRecommendationUsageRate(Long userId, Instant startUtc, Instant endUtc) {
        Long totalTransactions = transactionRepository.countTotalTransactionsByMonth(userId, startUtc, endUtc);
        Long recommendationUsedTransactions = transactionRepository.countRecommendationUsedTransactions(userId, startUtc, endUtc);

        if (totalTransactions == 0) {
            return 0.0;
        }

        return recommendationUsedTransactions.doubleValue() / totalTransactions.doubleValue();
    }

    private Double calculateSavingRate(Long saved, Long spent) {
        if (spent == null || spent == 0) {
            return 0.0;
        }
        return (saved.doubleValue() / spent.doubleValue()) * 100.0;
    }

    private Double calculateSpendingRatio(Long spent, Long totalSpent) {
        if (totalSpent == null || totalSpent == 0) {
            return 0.0;
        }
        return (spent.doubleValue() / totalSpent.doubleValue()) * 100.0;
    }

    private BigDecimal calculateUsageRate(Long used, Long total) {
        if (total == null || total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(used)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private Long calculateAverageSaving(Long totalSaved, Long count) {
        if (count == null || count == 0) {
            return 0L;
        }
        return totalSaved / count;
    }

    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "기타";
        }

        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("기타");
    }

    private Instant[] getStartEndUtc(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        ZoneId kst = ZoneId.of("Asia/Seoul");
        Instant startUtc = ym.atDay(1).atStartOfDay(kst).toInstant();
        Instant endUtc   = ym.plusMonths(1).atDay(1).atStartOfDay(kst).toInstant();
        return new Instant[]{startUtc, endUtc};
    }
}
