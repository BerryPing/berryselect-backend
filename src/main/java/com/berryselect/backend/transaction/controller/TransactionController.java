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
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    // 내 거래 내역 조회
    @GetMapping("/list")
    public ResponseEntity<Page<TransactionDetailResponse>> getUserTransactions(
            @AuthenticationPrincipal String subject,
            @RequestParam(value = "yearMonth", required = false) String yearMonth,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "txTime") Pageable pageable) {

        // JWT subject에서 userId 추출
        // Long userId = Long.parseLong(subject);
        Long userId;
        if (subject == null || "anonymousUser".equals(subject)) {
            userId = 2L;  // 테스트용 기본 계정
        } else {
            userId = Long.parseLong(subject);
        }

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

    // 월별 추천 사용률 조회
    @GetMapping("/recommendation-rate")
    public ResponseEntity<Double> getRecommendationUsageRate(
            @AuthenticationPrincipal String subject,
            @RequestParam("yearMonth") String yearMonth) {

        // JWT subject에서 userId 추출
        //Long userId = Long.parseLong(subject);
        Long userId;
        if (subject == null || "anonymousUser".equals(subject)) {
            userId = 2L;  // 테스트용 기본 계정
        } else {
            userId = Long.parseLong(subject);
        }

        log.info("추천 사용률 조회 - userId: {}, yearMonth: {}", userId, yearMonth);

        try {
            Double usageRate = transactionService.getRecommendationUsageRate(userId, yearMonth);
            return ResponseEntity.ok(usageRate);

        } catch (Exception e) {
            log.error("추천 사용률 조회 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 월별 총 절약금액 조회
    @GetMapping("/total-saved")
    public ResponseEntity<Long> getTotalSavedAmount(
            @AuthenticationPrincipal String subject,
            @RequestParam("yearMonth") String yearMonth) {

        // JWT subject에서 userId 추출
        //Long userId = Long.parseLong(subject);
        Long userId;
        if (subject == null || "anonymousUser".equals(subject)) {
            userId = 2L;  // 테스트용 기본 계정
        } else {
            userId = Long.parseLong(subject);
        }

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
