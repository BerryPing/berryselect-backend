package com.berryselect.backend.transaction.controller;

import com.berryselect.backend.security.dto.AuthUser;
import com.berryselect.backend.transaction.dto.request.TransactionRequest;
import com.berryselect.backend.transaction.dto.response.TransactionDetailResponse;
import com.berryselect.backend.transaction.dto.response.TransactionResponse;
import com.berryselect.backend.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(value = "yearMonth", required = false) String yearMonth,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "txTime") Pageable pageable) {

        Long userId = null;
        try {
            if (authUser == null) {
                log.error("인증 정보가 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            userId = authUser.getId();

            log.info("거래 내역 조회 요청 - userId: {}, yearMonth: {}, categoryId: {}, page: {}",
                    userId, yearMonth, categoryId, pageable.getPageNumber());

            Page<TransactionDetailResponse> transactions =
                    transactionService.getUserTransactions(userId, yearMonth, categoryId, pageable);

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
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("yearMonth") String yearMonth) {

        Long userId = null;
        try {
            if (authUser == null) {
                log.error("인증 정보가 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            userId = authUser.getId();

            log.info("추천 사용률 조회 - userId: {}, yearMonth: {}", userId, yearMonth);

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
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("yearMonth") String yearMonth) {

        Long userId = null;
        try {
            if (authUser == null) {
                log.error("인증 정보가 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            userId = authUser.getId();

            log.info("총 절약금액 조회 - userId: {}, yearMonth: {}", userId, yearMonth);

            Long totalSaved = transactionService.getTotalSavedAmount(userId, yearMonth);
            return ResponseEntity.ok(totalSaved);

        } catch (Exception e) {
            log.error("총 절약금액 조회 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 거래 생성 (팀원 로직/리턴은 유지, 시큐리티 연동만 변경)
    @PostMapping
    public TransactionResponse createTransaction(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody TransactionRequest request) {

        Long userId = null;
        try {
            if (authUser == null) {
                log.error("인증 정보가 없습니다");
                throw new org.springframework.security.access.AccessDeniedException("인증이 필요합니다.");
            }
            userId = authUser.getId();

            log.info("거래 생성 요청 - userId: {}", userId);
            return transactionService.createTransaction(request, userId);

        } catch (RuntimeException e) {
            log.error("거래 생성 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("거래 생성 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}


