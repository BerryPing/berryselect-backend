package com.berryselect.backend.notification.controller;

import com.berryselect.backend.notification.service.NotificationSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

        private final NotificationSchedulerService schedulerService;

        // 기프티콘 만료 체크 수동 실행
        @PostMapping("/run/gifticon-expiration")
        public ResponseEntity<String> runGifticonExpirationCheck() {
            schedulerService.runGifticonExpirationCheck();
            return ResponseEntity.ok("기프티콘 만료 알림 배치 실행 완료");
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
