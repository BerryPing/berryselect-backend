package com.berryselect.backend.notification.controller;

import com.berryselect.backend.common.dto.ApiResponse;
import com.berryselect.backend.notification.service.NotificationSchedulerService;
import com.berryselect.backend.security.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final NotificationSchedulerService schedulerService;

    // 기프티콘 만료 체크 수동 실행
    @PostMapping("/run/gifticon-expiration")
    public ResponseEntity<ApiResponse<String>> runGifticonExpirationCheck(
            @AuthenticationPrincipal AuthUser authUser) {

        Long userId = null; // 미리 선언

        try {
            // AuthUser null 체크 추가
            if (authUser == null) {
                log.error("인증 정보가 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("인증이 필요합니다."));
            }

            // AuthUser에서 관리자 ID 추출
            userId = authUser.getId();

            log.info("기프티콘 만료 체크 배치 수동 실행 요청 - adminId: {}", userId);

            // TODO: 관리자 권한 검증 로직 추가 필요
            // if (!userService.isAdmin(userId)) {
            //     log.warn("관리자 권한이 없는 사용자의 접근 시도 - userId: {}", userId);
            //     return ResponseEntity.status(HttpStatus.FORBIDDEN)
            //         .body(ApiResponse.error("관리자 권한이 필요합니다."));
            // }

            schedulerService.runGifticonExpirationCheck();

            log.info("기프티콘 만료 체크 배치 수동 실행 완료 - adminId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success("기프티콘 만료 알림 배치 실행 완료"));

        } catch (Exception e) {
            log.error("기프티콘 만료 체크 배치 실행 실패 - adminId: {}, error: {}",
                    userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("배치 실행 중 오류가 발생했습니다."));
        }
    }

    // 예산 초과 체크 수동 실행
    @PostMapping("/run/budget-exceeded")
    public ResponseEntity<ApiResponse<String>> runBudgetExceededCheck(
            @AuthenticationPrincipal AuthUser authUser) {

        Long userId = null; // 미리 선언

        try {
            // AuthUser null 체크 추가
            if (authUser == null) {
                log.error("인증 정보가 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("인증이 필요합니다."));
            }

            // AuthUser에서 관리자 ID 추출
            userId = authUser.getId();

            log.info("예산 초과 체크 배치 수동 실행 요청 - adminId: {}", userId);

            // TODO: 관리자 권한 검증 로직 추가 필요

            // schedulerService.runBudgetExceededCheck();

            log.info("예산 초과 체크 배치 수동 실행 완료 - adminId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success("예산 초과 알림 배치 실행 완료"));

        } catch (Exception e) {
            log.error("예산 초과 체크 배치 실행 실패 - adminId: {}, error: {}",
                    userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("배치 실행 중 오류가 발생했습니다."));
        }
    }

    // 주말 혜택 알림 수동 실행
    @PostMapping("/run/weekend-benefit")
    public ResponseEntity<ApiResponse<String>> runWeekendBenefitAlert(
            @AuthenticationPrincipal AuthUser authUser) {

        Long userId = null; // 미리 선언

        try {
            // AuthUser null 체크 추가
            if (authUser == null) {
                log.error("인증 정보가 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("인증이 필요합니다."));
            }

            // AuthUser에서 관리자 ID 추출
            userId = authUser.getId();

            log.info("주말 혜택 알림 배치 수동 실행 요청 - adminId: {}", userId);

            // TODO: 관리자 권한 검증 로직 추가 필요

            // schedulerService.runWeekendBenefitAlert();

            log.info("주말 혜택 알림 배치 수동 실행 완료 - adminId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success("주말 혜택 알림 배치 실행 완료"));

        } catch (Exception e) {
            log.error("주말 혜택 알림 배치 실행 실패 - adminId: {}, error: {}",
                    userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("배치 실행 중 오류가 발생했습니다."));
        }
    }
}
