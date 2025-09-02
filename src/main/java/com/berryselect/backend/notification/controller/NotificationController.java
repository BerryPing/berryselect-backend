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
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Notification> notificatons = notificationService.getUserNotifications(userId, pageable);
        Page<NotificationResponse> responses = notificatons.map(NotificationResponse::from);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 읽지 않은 알림 개수 조회
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal Long userId) {
        Long unreadCount = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(unreadCount));
    }

    // 알림 읽음 처리
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(@PathVariable Long id
            , @AuthenticationPrincipal Long userId) {

        notificationService.markNotificationAsRead(id, userId);

        log.info("알림 읽음 처리 요청 - notificationId: {}, userId: {}", id, userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 관리자용 알림 발송 테스트
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendTestNotification(
            @RequestParam Long targetUserId,
            @AuthenticationPrincipal Long adminUserId) {

        // TODO: 관리자 권한 체크
        Notification notification = notificationService.sendTestNotification(targetUserId);
        NotificationResponse response = NotificationResponse.from(notification);

        log.info("테스트 알림 발송 요청 - targetUserId: {}, notificationId: {}", targetUserId, adminUserId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
