package com.berryselect.backend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {
    // 카테고리 선택
    private List<String> preferredCategories;

    // 알림/마케팅 설정
    private Boolean warnOverBudget;
    private Boolean gifticonExpireAlert;
    private Boolean eventAlert;
    private Boolean allowKakaoAlert;
    private Boolean marketingOptIn;
}
