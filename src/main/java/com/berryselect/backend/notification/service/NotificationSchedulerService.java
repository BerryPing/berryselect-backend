package com.berryselect.backend.notification.service;

import com.berryselect.backend.auth.domain.User;
import com.berryselect.backend.auth.repository.UserRepository;
import com.berryselect.backend.wallet.domain.UserAsset;
import com.berryselect.backend.wallet.repository.UserAssetRepository;
import com.berryselect.backend.budget.repository.MonthlyBudgetRepository;
import com.berryselect.backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class NotificationSchedulerService {

    private final UserAssetRepository userAssetRepository;
    private final UserRepository userRepository;
    //private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Value("${notification.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    /**
     * 매일 오전 9시에 기프티콘 만료 알림 체크
     */
    @Scheduled(cron = "${notification.scheduler.gifticon-expiration.cron:0 0 9 * * *}")
    @Transactional(readOnly = false)
    public void checkGifticonExpiration() {
        log.info("=== 기프티콘 만료 알림 배치 시작 ===");

        try {
            // 1일 후 만료되는 기프티콘들 조회
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<UserAsset> expiringGifticons = userAssetRepository
                    .findExpiringGifticons(tomorrow);

            log.info("만료 예정 기프티콘 {}개 발견", expiringGifticons.size());

            for (UserAsset gifticon : expiringGifticons) {
                try {
                    sendGifticonExpirationNotification(gifticon, 1);
                } catch (Exception e) {
                    log.error("기프티콘 만료 알림 발송 실패 - assetId: {}, userId: {}",
                            gifticon.getId(), gifticon.getUserId(), e);
                }
            }

            // 3일 후 만료되는 기프티콘들도 체크 (3일 전 알림)
            LocalDate threeDaysLater = LocalDate.now().plusDays(3);
            List<UserAsset> threeDayWarning = userAssetRepository
                    .findExpiringGifticons(threeDaysLater);

            log.info("3일 후 만료 예정 기프티콘 {}개 발견", threeDayWarning.size());

            for (UserAsset gifticon : threeDayWarning) {
                try {
                    sendGifticonExpirationNotification(gifticon, 3);
                } catch (Exception e) {
                    log.error("기프티콘 3일 전 알림 발송 실패 - assetId: {}, userId: {}",
                            gifticon.getId(), gifticon.getUserId(), e);
                }
            }

            log.info("=== 기프티콘 만료 알림 배치 완료 ===");

        } catch (Exception e) {
            log.error("기프티콘 만료 알림 배치 실행 중 오류 발생", e);
        }
    }

    /**
     * 매일 오후 8시에 예산 초과 알림 체크
     */
//    @Scheduled(cron = "0 0 20 * * *")
//    @Transactional(readOnly = false)
//    public void checkBudgetExceeded() {
//        log.info("=== 예산 초과 알림 배치 시작 ===");
//
//        try {
//            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//
//            // 이번 달 예산을 설정한 모든 사용자 조회
//            var budgetUsers = monthlyBudgetRepository.findByYearMonth(currentMonth);
//
//            log.info("예산 설정 사용자 {}명 확인", budgetUsers.size());
//
//            for (var budget : budgetUsers) {
//                try {
//                    checkUserBudgetStatus(budget.getUser().getId(), budget.getAmountTarget(), currentMonth);
//                } catch (Exception e) {
//                    log.error("사용자 예산 체크 실패 - userId: {}", budget.getUser().getId(), e);
//                }
//            }
//
//            log.info("=== 예산 초과 알림 배치 완료 ===");
//
//        } catch (Exception e) {
//            log.error("예산 초과 알림 배치 실행 중 오류 발생", e);
//        }
//    }

    /**
     * 주말 저녁 7시에 혜택 이벤트 알림 발송 (금요일)
     */
//    @Scheduled(cron = "0 0 19 * * FRI")
//    @Transactional(readOnly = false)
//    public void sendWeekendBenefitAlert() {
//        log.info("=== 주말 혜택 알림 배치 시작 ===");
//
//        try {
//            // 활성 사용자들에게 주말 혜택 알림 발송
//            List<User> activeUsers = userRepository.findActiveUsers();
//
//            String eventTitle = "주말 특별 혜택";
//            String eventDetail = "이번 주말 카페, 음식점에서 특별 할인 혜택을 놓치지 마세요!";
//
//            for (User user : activeUsers) {
//                try {
//                    notificationService.sendBenefitEventAlert(user.getId(), eventTitle, eventDetail);
//                    Thread.sleep(100); // API 호출 간격 조절
//                } catch (Exception e) {
//                    log.error("주말 혜택 알림 발송 실패 - userId: {}", user.getId(), e);
//                }
//            }
//
//            log.info("=== 주말 혜택 알림 배치 완료 - {}명에게 발송 ===", activeUsers.size());
//
//        } catch (Exception e) {
//            log.error("주말 혜택 알림 배치 실행 중 오류 발생", e);
//        }
//    }

    private void sendGifticonExpirationNotification(UserAsset gifticon, int daysLeft) {
        String gifticonName = gifticon.getProduct().getName();
        Long userId = gifticon.getUserId();

        notificationService.sendGifticonExpireAlert(userId, gifticonName, daysLeft);

        log.info("기프티콘 만료 알림 발송 완료 - userId: {}, gifticon: {}, daysLeft: {}",
                userId, gifticonName, daysLeft);
    }

//    private void checkUserBudgetStatus(Long userId, int budgetTarget, String yearMonth) {
//        // 이번 달 총 지출 계산
//        Long totalSpent = transactionRepository.getTotalSpentByUserAndMonth(userId, yearMonth);
//
//        if (totalSpent == null) totalSpent = 0L;
//
//        // 예산 80% 초과시 알림
//        double threshold = budgetTarget * 0.8;
//        if (totalSpent >= threshold && totalSpent < budgetTarget) {
//            // 80% 초과 경고
//            String budgetInfo = String.format("예산 %,d원 중 %,d원 사용 (%.1f%% 달성)",
//                    budgetTarget, totalSpent, ((double)totalSpent / budgetTarget) * 100);
//
//            if (!isAlreadyNotified(userId, "BUDGET_WARNING", yearMonth)) {
//                notificationService.sendBudgetWarning(userId, budgetInfo);
//                log.info("예산 80% 경고 알림 발송 - userId: {}, spent: {}, budget: {}",
//                        userId, totalSpent, budgetTarget);
//            }
//        }
//        // 예산 100% 초과시 알림
//        else if (totalSpent >= budgetTarget) {
//            String budgetInfo = String.format("예산 %,d원 중 %,d원 사용 (%.1f%% 초과)",
//                    budgetTarget, totalSpent, ((double)(totalSpent - budgetTarget) / budgetTarget) * 100);
//
//            if (!isAlreadyNotified(userId, "BUDGET_EXCEEDED", yearMonth)) {
//                notificationService.sendBudgetAlert(userId, budgetInfo);
//                log.info("예산 초과 알림 발송 - userId: {}, spent: {}, budget: {}",
//                        userId, totalSpent, budgetTarget);
//            }
//        }
//    }

    /**
     * 수동 실행용 메서드들 (테스트용!!!)
     */
    @Transactional(readOnly = false)
    public void runGifticonExpirationCheck() {
        log.info("수동 실행: 기프티콘 만료 체크");
        checkGifticonExpiration();
    }

//    public void runBudgetExceededCheck() {
//        log.info("수동 실행: 예산 초과 체크");
//        checkBudgetExceeded();
//    }

//    public void runWeekendBenefitAlert() {
//        log.info("수동 실행: 주말 혜택 알림");
//        sendWeekendBenefitAlert();
//    }
}