package com.berryselect.backend.notification.controller;

import com.berryselect.backend.common.dto.ApiResponse;
import com.berryselect.backend.notification.service.NotificationSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            @AuthenticationPrincipal String subject) {

        try {
            // JWT subject에서 관리자 ID 추출
            Long userId = Long.parseLong(subject);

            log.info("기프티콘 만료 체크 배치 수동 실행 요청 - userId: {}", userId);

            schedulerService.runGifticonExpirationCheck();

            log.info("기프티콘 만료 체크 배치 수동 실행 완료 - userId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success("기프티콘 만료 알림 배치 실행 완료"));

        } catch (NumberFormatException e) {
            log.error("유효하지 않은 관리자 ID - subject: {}", subject, e);
            return ResponseEntity.badRequest().body(ApiResponse.error("유효하지 않은 사용자 ID"));

        } catch (Exception e) {
            log.error("기프티콘 만료 체크 배치 실행 실패 - subject: {}, error: {}",
                    subject, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("배치 실행 중 오류가 발생했습니다."));
        }
    }

        // 예산 초과 체크 수동 실행
//        @PostMapping("/run/budget-exceeded")
//        public ResponseEntity<String> runBudgetExceededCheck() {
//            schedulerService.runBudgetExceededCheck();
//            return ResponseEntity.ok("예산 초과 알림 배치 실행 완료");
//        }

        // 주말 혜택 알림 수동 실행
//        @PostMapping("/run/weekend-benefit")
//        public ResponseEntity<String> runWeekendBenefitAlert() {
//            schedulerService.runWeekendBenefitAlert();
//            return ResponseEntity.ok("주말 혜택 알림 배치 실행 완료");
//        }

}
