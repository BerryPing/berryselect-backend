package com.berryselect.backend.budget.controller;

import com.berryselect.backend.budget.dto.response.MonthlyReportDetailResponse;
import com.berryselect.backend.budget.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 예산 및 리포트 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/myberry")
@RequiredArgsConstructor
@Slf4j
public class BudgetReportController {

    private final ReportService reportService;

    /**
     * 월별 상세 리포트 조회
     *
     * @param yearMonth 조회 년월 (YYYY-MM)
     * @param userId 사용자 ID
     * @return 월별 상세 리포트 (소비 차트, 절감액, 추천 사용률, AI 요약 포함)
     *
     * 포함 데이터:
     * - 총 지출액, 총 절약액, 총 거래건수
     * - 카테고리별 지출 내역 (금액, 건수, 비율, 절약률)
     * - 추천 사용률 통계 (사용 건수, 비율, 절약 효과)
     * - AI 분석 요약 (소비 패턴, 절약 제안)
     * - 전체 절약률
     *
     * 예시:
     * GET /myberry/reports/2024-12?userId=1
     */
    @GetMapping("/reports/{yearMonth}")
    public ResponseEntity<MonthlyReportDetailResponse> getMonthlyReport(
            @PathVariable("yearMonth") String yearMonth,
            @RequestParam("userId") Long userId) {

        log.info("월별 리포트 조회 요청 - userId: {}, yearMonth: {}", userId, yearMonth);

        try {
            // 년월 형식 검증
            if (!isValidYearMonth(yearMonth)) {
                log.warn("잘못된 년월 형식 - yearMonth: {}", yearMonth);
                return ResponseEntity.badRequest().build();
            }

            MonthlyReportDetailResponse report = reportService.getMonthlyReportDetail(userId, yearMonth);

            log.info("월별 리포트 조회 완료 - userId: {}, yearMonth: {}, 총지출: {}원",
                    userId, yearMonth, report.getTotalSpent());

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            log.error("월별 리포트 조회 실패 - userId: {}, yearMonth: {}, error: {}",
                    userId, yearMonth, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * AI 분석 리포트 재생성 (별도 API)
     *
     * @param yearMonth 조회 년월 (YYYY-MM)
     * @param userId 사용자 ID
     * @return 새로 생성된 AI 분석 내용
     */
    @PostMapping("/reports/{yearMonth}/ai-regenerate")
    public ResponseEntity<String> regenerateAiSummary(
            @PathVariable("yearMonth") String yearMonth,
            @RequestParam("userId") Long userId) {

        log.info("AI 분석 재생성 요청 - userId: {}, yearMonth: {}", userId, yearMonth);

        try {
            if (!isValidYearMonth(yearMonth)) {
                return ResponseEntity.badRequest().build();
            }

            String aiSummary = reportService.generateOrGetAiSummary(userId, yearMonth);

            if (aiSummary.contains("AI 분석을 생성할 수 없습니다")) {
                log.warn("AI 분석 생성 실패 - userId: {}, yearMonth: {}", userId, yearMonth);
                return ResponseEntity.internalServerError().body(aiSummary);
            }

            log.info("AI 분석 재생성 완료 - userId: {}, yearMonth: {}", userId, yearMonth);
            return ResponseEntity.ok(aiSummary);

        } catch (Exception e) {
            log.error("AI 분석 재생성 실패 - userId: {}, yearMonth: {}, error: {}",
                    userId, yearMonth, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("AI 분석을 생성하는 중 오류가 발생했습니다.");
        }
    }

    /**
     * 년월 형식 검증 (YYYY-MM)
     */
    private boolean isValidYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.length() != 7) {
            return false;
        }

        try {
            String[] parts = yearMonth.split("-");
            if (parts.length != 2) {
                return false;
            }

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            return year >= 2020 && year <= 2030 && month >= 1 && month <= 12;

        } catch (NumberFormatException e) {
            return false;
        }
    }
}
