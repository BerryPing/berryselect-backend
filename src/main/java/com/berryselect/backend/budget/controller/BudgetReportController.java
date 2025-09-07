package com.berryselect.backend.budget.controller;

import com.berryselect.backend.budget.dto.response.MonthlyReportDetailResponse;
import com.berryselect.backend.budget.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // 월별 상세 리포트 조회
    @GetMapping("/reports/{yearMonth}")
    public ResponseEntity<MonthlyReportDetailResponse> getMonthlyReport(
            @AuthenticationPrincipal String subject,
            @PathVariable("yearMonth") String yearMonth) {

        try {
            // JWT subject에서 userId 추출
            Long userId = Long.parseLong(subject);

            log.info("월별 리포트 조회 요청 - userId: {}, yearMonth: {}", userId, yearMonth);

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
            log.error("월별 리포트 조회 실패 - subject: {}, yearMonth: {}, error: {}",
                    subject, yearMonth, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // AI 분석 리포트 재생성 (별도 API)
    @PostMapping("/reports/{yearMonth}/ai-regenerate")
    public ResponseEntity<String> regenerateAiSummary(
            @AuthenticationPrincipal String subject,
            @PathVariable("yearMonth") String yearMonth) {

        try {
            // JWT subject에서 userId 추출
            Long userId = Long.parseLong(subject);

            log.info("AI 분석 재생성 요청 - userId: {}, yearMonth: {}", userId, yearMonth);

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
            log.error("AI 분석 재생성 실패 - subject: {}, yearMonth: {}, error: {}",
                    subject, yearMonth, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("AI 분석을 생성하는 중 오류가 발생했습니다.");
        }
    }

    // 년월 형식 검증 (YYYY-MM)
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
