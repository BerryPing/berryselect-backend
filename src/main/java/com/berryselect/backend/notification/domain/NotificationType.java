package com.berryselect.backend.notification.domain;

// 알림 유형 enum
public enum NotificationType {

    // 예산 초과 알림
    BUDGET_ALERT("예산 초과 알림", "BUDGET_ALERT"),

    // 기프티콘 만료 알림
    GIFTICON_EXPIRE("기프티콘 만료 알림", "GIFICON_EXPIRE"),

    // 혜택/이벤트 알림
    BENEFIT_EVENT("혜택/이벤트 알림", "BENEFIT_EVENT");

    private final String displayName;
    private final String code;

    NotificationType(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    // 코드로 NotificationType 찾기
    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 알림 타입 코드: " + code);
    }
}
