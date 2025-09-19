package com.berryselect.backend.notification.domain;

// 알림 발송 상태 enum
public enum NotificationStatus {

    // 발송 대기
    PENDING("발송 대기", "PENDING"),

    // 발송 완료
    SENT("발송 완료", "SENT"),

    // 발송 실패
    FAILED("발송 실패", "FAILED");

    private final String displayName;
    private final String code;

    NotificationStatus(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    // 코드로 NotificationStatus 찾기
    public static NotificationStatus fromCode(String code) {
        for (NotificationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 알림 상태 코드: " + code);
    }

    // 재시도 가능한지 확인
    public boolean isRetryable() {
        return this == FAILED;
    }
}
