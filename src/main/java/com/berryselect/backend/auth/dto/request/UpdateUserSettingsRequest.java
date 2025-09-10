package com.berryselect.backend.auth.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserSettingsRequest {
    // 알림/마케팅
    private Boolean warnOverBudget;
    private Boolean gifticonExpireAlert;
    private Boolean eventAlert;
    private Boolean allowKakaoAlert;
    private Boolean marketingOptIn;

    // 선호 카테고리
    private List<String> preferredCategories;
    private List<Long> preferredCategoryIds;
}
