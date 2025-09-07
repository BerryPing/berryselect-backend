package com.berryselect.backend.transaction.controller;

import com.berryselect.backend.transaction.dto.response.TransactionDetailResponse;
import com.berryselect.backend.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 거래 내역 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/myberry")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 내 거래 내역 조회 (필터링 + 페이징)
     *
     * @param userId 사용자 ID
     * @param yearMonth 조회 년월 (YYYY-MM, 선택사항)
     * @param categoryId 카테고리 ID (선택사항)
     * @param pageable 페이징 정보 (기본: 20개씩)
     * @return 거래 상세 내역 목록 (페이징)
     *
     * 예시:
     * GET /myberry/transactions?userId=1&yearMonth=2024-12&categoryId=1&page=0&size=20
     * GET /myberry/transactions?userId=1&yearMonth=2024-12
     * GET /myberry/transactions?userId=1
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionDetailResponse>> getUserTransactions(
            @AuthenticationPrincipal String subject,
            @RequestParam(value = "yearMonth", required = false) String yearMonth,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "txTime") Pageable pageable) {

        // JWT subject에서 userId 추출
        Long userId = Long.parseLong(subject);

        log.info("거래 내역 조회 요청 - userId: {}, yearMonth: {}, categoryId: {}, page: {}",
                userId, yearMonth, categoryId, pageable.getPageNumber());

        try {
            Page<TransactionDetailResponse> transactions = transactionService
                    .getUserTransactions(userId, yearMonth, categoryId, pageable);

            log.info("거래 내역 조회 완료 - 총 {}건, 현재 페이지 {}건",
                    transactions.getTotalElements(), transactions.getNumberOfElements());

            return ResponseEntity.ok(transactions);

        } catch (Exception e) {
            log.error("거래 내역 조회 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 월별 추천 사용률 조회 (별도 API)
     *
     * @param userId 사용자 ID
     * @param yearMonth 조회 년월 (YYYY-MM)
     * @return 추천 사용률 (0.0 ~ 1.0)
     */
    @GetMapping("/transactions/recommendation-rate")
    public ResponseEntity<Double> getRecommendationUsageRate(
            @AuthenticationPrincipal String subject,
            @RequestParam("yearMonth") String yearMonth) {

        // JWT subject에서 userId 추출
        Long userId = Long.parseLong(subject);

        log.info("추천 사용률 조회 - userId: {}, yearMonth: {}", userId, yearMonth);

        try {
            Double usageRate = transactionService.getRecommendationUsageRate(userId, yearMonth);
            return ResponseEntity.ok(usageRate);

        } catch (Exception e) {
            log.error("추천 사용률 조회 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 월별 총 절약금액 조회 (별도 API)
     *
     * @param userId 사용자 ID
     * @param yearMonth 조회 년월 (YYYY-MM)
     * @return 총 절약금액
     */
    @GetMapping("/transactions/total-saved")
    public ResponseEntity<Long> getTotalSavedAmount(
            @AuthenticationPrincipal String subject,
            @RequestParam("yearMonth") String yearMonth) {

        // JWT subject에서 userId 추출
        Long userId = Long.parseLong(subject);

        log.info("총 절약금액 조회 - userId: {}, yearMonth: {}", userId, yearMonth);

        try {
            Long totalSaved = transactionService.getTotalSavedAmount(userId, yearMonth);
            return ResponseEntity.ok(totalSaved);

        } catch (Exception e) {
            log.error("총 절약금액 조회 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
