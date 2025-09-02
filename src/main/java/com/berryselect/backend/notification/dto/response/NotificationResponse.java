package com.berryselect.backend.notification.dto.response;

import com.berryselect.backend.notification.domain.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Notification DTO(클라이언트용)
@Getter
@Builder
public class NotificationResponse {
    private Long notificationId;
    private String notificationType;
    private String title;
    private String body;
    private LocalDateTime sentAt;
    private String status;
    private LocalDateTime createAt;
    private Boolean isRead;

    // Notification Entity (내부 로직용) -> DTO(클라이언트용) 변환
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType().getDisplayName())
                .title(notification.getTitle())
                .body(notification.getBody())
                .sentAt(notification.getSentAt())
                .status(notification.getStatus().getDisplayName())
                .createAt(notification.getCreatedAt())
                .isRead(notification.getIsRead())
                .build();
    }
    // 알림 타입 한글명 반환
    public String getNotificationTypeDisplayName() {
        return this.notificationType;
    }

    // 발송 상태 한글명 반환
    public String getStatusDisplayName() {
        return this.status;
    }
}
