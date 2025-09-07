package com.berryselect.backend.notification.controller;

import com.berryselect.backend.common.dto.ApiResponse;
import com.berryselect.backend.notification.domain.Notification;
import com.berryselect.backend.notification.dto.response.NotificationResponse;
import com.berryselect.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림 이력 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal String subject,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        try {
            // JWT subject에서 userId 추출
            Long userId = Long.parseLong(subject);

            log.info("알림 이력 조회 요청 - userId: {}, page: {}", userId, pageable.getPageNumber());

            Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
            Page<NotificationResponse> responses = notifications.map(NotificationResponse::from);

            log.info("알림 이력 조회 완료 - userId: {}, 총 {}건, 현재 페이지 {}건",
                    userId, notifications.getTotalElements(), notifications.getNumberOfElements());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (NumberFormatException e) {
            log.error("유효하지 않은 사용자 ID - subject: {}", subject, e);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("알림 이력 조회 실패 - subject: {}, error: {}", subject, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 읽지 않은 알림 개수 조회
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal String subject) {
        try {
            // JWT subject에서 userId 추출
            Long userId = Long.parseLong(subject);

            log.info("읽지 않은 알림 개수 조회 - userId: {}", userId);

            Long unreadCount = notificationService.getUnreadCount(userId);

            log.info("읽지 않은 알림 개수 조회 완료 - userId: {}, count: {}", userId, unreadCount);

            return ResponseEntity.ok(ApiResponse.success(unreadCount));

        } catch (NumberFormatException e) {
            log.error("유효하지 않은 사용자 ID - subject: {}", subject, e);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 실패 - subject: {}, error: {}", subject, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 알림 읽음 처리
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(
            @AuthenticationPrincipal String subject,
            @PathVariable Long id) {

        try {
            // JWT subject에서 userId 추출
            Long userId = Long.parseLong(subject);

            log.info("알림 읽음 처리 요청 - notificationId: {}, userId: {}", id, userId);

            notificationService.markNotificationAsRead(id, userId);

            log.info("알림 읽음 처리 완료 - notificationId: {}, userId: {}", id, userId);

            return ResponseEntity.ok(ApiResponse.success(null));

        } catch (NumberFormatException e) {
            log.error("유효하지 않은 사용자 ID - subject: {}", subject, e);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("알림 읽음 처리 실패 - notificationId: {}, subject: {}, error: {}",
                    id, subject, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 알림 발송 테스트
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendTestNotification(
            @AuthenticationPrincipal String subject) {

        try {
            // JWT subject에서 userId 추출 (본인에게만 발송)
            Long userId = Long.parseLong(subject);

            log.info("테스트 알림 발송 요청 - userId: {} (본인에게 발송)", userId);

            // 본인에게만 테스트 알림 발송
            Notification notification = notificationService.sendTestNotification(userId);
            NotificationResponse response = NotificationResponse.from(notification);

            log.info("테스트 알림 발송 완료 - userId: {}, notificationId: {}",
                    userId, notification.getId());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (NumberFormatException e) {
            log.error("유효하지 않은 사용자 ID - subject: {}", subject, e);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("테스트 알림 발송 실패 - subject: {}, error: {}",
                    subject, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
