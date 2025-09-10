package com.berryselect.backend.auth.service;

import com.berryselect.backend.auth.domain.User;
import com.berryselect.backend.auth.domain.UserPreferredCategory;
import com.berryselect.backend.auth.domain.UserSettings;
import com.berryselect.backend.auth.dto.request.UpdateUserSettingsRequest;
import com.berryselect.backend.auth.dto.response.UserSettingsResponse;
import com.berryselect.backend.auth.repository.UserPreferredCategoryRepository;
import com.berryselect.backend.auth.repository.UserRepository;
import com.berryselect.backend.auth.repository.UserSettingsRepository;
import com.berryselect.backend.merchant.domain.Category;
import com.berryselect.backend.merchant.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UserPreferredCategoryRepository userPreferredCategoryRepository;
    private final CategoryRepository categoryRepository;

    /** GET /users/me/settings */
    public UserSettingsResponse getSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findById(userId).orElse(null);

        List<String> categoryNames =
                userPreferredCategoryRepository.findCategoryNamesByUserId(userId);

        return UserSettingsResponse.builder()
                .preferredCategories(categoryNames)
                .warnOverBudget(bool(settings != null ? settings.getNotifyBudgetAlert() : null))
                .gifticonExpireAlert(bool(settings != null ? settings.getNotifyGifticonExpire() : null))
                .eventAlert(bool(settings != null ? settings.getNotifyBenefitEvents() : null))
                .allowKakaoAlert(bool(settings != null ? settings.getAllowKakaoAlert() : null))
                .marketingOptIn(bool(settings != null ? settings.getMarketingOption() : null))
                .build();
    }

    /** PUT /users/me/settings */
    @Transactional
    public UserSettingsResponse updateSettings(Long userId, UpdateUserSettingsRequest req) {
        // 1) UserSettings upsert
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseGet(() -> {
                    UserSettings us = new UserSettings();
                    us.setUserId(userId);
                    us.setUser(userRepository.getReferenceById(userId));
                    return us;
                });

        settings.setNotifyBudgetAlert(req.getWarnOverBudget());
        settings.setNotifyGifticonExpire(req.getGifticonExpireAlert());
        settings.setNotifyBenefitEvents(req.getEventAlert());
        settings.setAllowKakaoAlert(req.getAllowKakaoAlert());
        settings.setMarketingOption(req.getMarketingOptIn());
        userSettingsRepository.save(settings);

        // 2) 선호 카테고리 재저장: 모두 삭제 후 다시 저장
        userPreferredCategoryRepository.deleteAllByUserId(userId);

        User userRef = userRepository.getReferenceById(userId);

        // (A) categoryId 리스트가 온 경우
        if (req.getPreferredCategoryIds() != null && !req.getPreferredCategoryIds().isEmpty()) {
            for (Long cid : req.getPreferredCategoryIds()) {
                Category catRef = categoryRepository.getReferenceById(cid);

                UserPreferredCategory upc = new UserPreferredCategory();
                upc.setUser(userRef);
                upc.setCategory(catRef);

                userPreferredCategoryRepository.save(upc);
            }
        }
        // (B) category 이름 리스트가 온 경우 → id 조회 후 저장
        else if (req.getPreferredCategories() != null && !req.getPreferredCategories().isEmpty()) {
            List<Long> ids = categoryRepository.findIdsByNames(req.getPreferredCategories());
            for (Long cid : ids) {
                Category catRef = categoryRepository.getReferenceById(cid);

                UserPreferredCategory upc = new UserPreferredCategory();
                upc.setUser(userRef);
                upc.setCategory(catRef);

                userPreferredCategoryRepository.save(upc);
            }
        }

        // 3) 응답
        List<String> categoryNames =
                userPreferredCategoryRepository.findCategoryNamesByUserId(userId);

        return UserSettingsResponse.builder()
                .preferredCategories(categoryNames)
                .warnOverBudget(bool(settings.getNotifyBudgetAlert()))
                .gifticonExpireAlert(bool(settings.getNotifyGifticonExpire()))
                .eventAlert(bool(settings.getNotifyBenefitEvents()))
                .allowKakaoAlert(bool(settings.getAllowKakaoAlert()))
                .marketingOptIn(bool(settings.getMarketingOption()))
                .build();
    }

    private static boolean bool(Boolean b) {
        return b != null && b;
    }
}