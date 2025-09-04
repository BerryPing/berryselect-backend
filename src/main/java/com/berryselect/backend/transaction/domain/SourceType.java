package com.berryselect.backend.transaction.domain;

public enum SourceType {
    CARD("카드"),
    MEMBERSHIP("멤버십"),
    GIFTICON("기프티콘");

    private final String koreanName;

    SourceType(String koreanName) {
        this.koreanName = koreanName;
    }
    public String getKoreanName() {
        return koreanName;
    }
}
