package com.berryselect.backend.notification.repository;

import com.berryselect.backend.notification.domain.Notification;
import com.berryselect.backend.notification.domain.NotificationStatus;
import com.berryselect.backend.notification.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 사용자별 알림 목록 조회 (최신순)
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 사용자별 읽지 않은 알림 개수 조회
    @Query("SELECT COUNT(n) Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);

    // 사용자별 읽지 않은 알림 목록 조회
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // 발송 실패한 알림 중 재시도 가능한 목록 조회
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retries < 3")
    List<Notification> findRetryableNotifications(@Param("status") NotificationStatus status);

    // 특정 기간 내 생성된 알림 조회
    List<Notification> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // 사용자별 특정 타입 알림 조회
    List<Notification> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(Long userId, NotificationType type);
}
