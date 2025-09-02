package com.berryselect.backend.notification.service;

import com.berryselect.backend.auth.domain.User;
import com.berryselect.backend.auth.repository.UserRepository;
import com.berryselect.backend.common.exception.ApiException;
import com.berryselect.backend.notification.domain.Notification;
import com.berryselect.backend.notification.domain.NotificationStatus;
import com.berryselect.backend.notification.domain.NotificationType;
import com.berryselect.backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final KaKaoNotificationAdapter kaKaoNotificationAdapter;

    // 사용자별 알림 목록 조회
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    // 알림 읽음 처리
    @Transactional
    public void markNotificationAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException("알림을 찾을 수 없습니다."));

        // 본인의 알림인지 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new ApiException("본인의 알림만 읽을 수 있습니다.");
        }

        // 이미 읽은 알림이면 무시
        if (notification.getIsRead()) {
            return;
        }

        // 읽음 처리
        notification.markAsRead();
        log.info("알림 읽음 처리 완료 - notificationId: {}, userId: {}, isRead: {}", notificationId, userId, notification.getIsRead());
    }

    // 읽지 않은 알림 개수 조회
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    // 알림 생성 및 발송
    @Transactional
    public Notification createAndSendNotification(Long userId, NotificationType type, String title, String body) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("사용자를 찾을 수 없습니다."));

        // 알림 생성
        Notification notification = Notification.builder()
                .user(user)
                .notificationType(type)
                .title(title)
                .body(body)
                .build();

        notification = notificationRepository.save(notification);

        // 비동기로 카카오 알림 발송 시도
        sendKakaoNotificationAsync(notification);

        log.info("알림 생성 완료 - notificationId: {}, userId: {}, type: {}",
                notification.getId(), userId, type);

        return notification;
    }

    // 테스트용 알림 발송 (관리자용)
    @Transactional
    public Notification sendTestNotification(Long userId) {
        return createAndSendNotification(
                userId,
                NotificationType.BENEFIT_EVENT,
                "테스트 알림",
                "관리자가 발송한 테스트 알림입니다."
        );
    }

    // 예산 초과 알림 발송
    @Transactional
    public void sendBudgetAlert(Long userId, String budgetInfo) {
        String title = "예산 초과 알림";
        String body = String.format("설정한 예산을 초과했습니다. %s", budgetInfo);
        createAndSendNotification(userId, NotificationType.BUDGET_ALERT, title, body);
    }

    // 기프티콘 만료 알림 발송
    @Transactional
    public void sendGifticonExpireAlert(Long userId, String gifticonName, int daysLeft) {
        String title = "기프티콘 만료 알림";
        String body = String.format("%s 기프티콘이 %d일 후 만료됩니다.", gifticonName, daysLeft);
        createAndSendNotification(userId, NotificationType.GIFTICON_EXPIRE, title, body);
    }

    // 비동기 카카오 알림 발송
    private void sendKakaoNotificationAsync(Notification notification) {
        try {
            if (!isKakaoNotificationEnabled(notification.getUser())) {
                log.info("카카오 알림이 비활성화된 사용자 - userId: {}", notification.getUser().getId());
                notification.markAsSent();
                return;
            }

            // 카카오 알림 발송
            String resultCode = kaKaoNotificationAdapter.sendNotification(notification);

            // 발송 성공 처리
            notification.setKakaoResult(resultCode);
            notification.markAsSent();

            log.info("카카오 알림 발송 성공 - notificationId: {}, resultCode: {}", notification.getId(), resultCode);
        } catch (Exception e) {
            // 발송 실패 처리
            notification.markAsFailed(e.getMessage());
            log.error("카카오 알림 발송 실패 - notificationId: {}", notification.getId(), e);
        }
    }

    // 사용자 카카오 알림 설정 확인
    private boolean isKakaoNotificationEnabled(User user) {
        // UserSettings에서 카카오 알림 설정 확인
        // TODO : UserSettingsService와 연동

        return true; // 임시로 true 반환
    }

    // 실패한 알림 재시도
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> retryableNotifications = notificationRepository
                .findRetryableNotifications(NotificationStatus.FAILED);

        for(Notification notification : retryableNotifications) {
            log.info("알림 재시도 - notificationId: {}, retryCount: {}",
                    notification.getId(), notification.getRetries());
            sendKakaoNotificationAsync(notification);
        }
    }
}




