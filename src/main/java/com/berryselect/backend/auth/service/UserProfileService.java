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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UserPreferredCategoryRepository userPreferredCategoryRepository;
    private final CategoryRepository categoryRepository;

    // GET /users/me/settings
    @Transactional
    public UserSettingsResponse getSettings(Long userId) {
        // 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        // 알림/마케팅 설정 읽기 (없을 수도 있음)
        var settings = userSettingsRepository.findByUserId(user.getId()).orElse(null);

        // 선호 카테고리 이름 리스트
        var preferredNames = userPreferredCategoryRepository.findCategoryNamesByUserId(user.getId());

        // 응답
        return new UserSettingsResponse(
                preferredNames,
                settings != null ? settings.getNotifyBudgetAlert()   : null,
                settings != null ? settings.getNotifyGifticonExpire(): null,
                settings != null ? settings.getNotifyBenefitEvents(): null,
                settings != null ? settings.getAllowKakaoAlert()     : null,
                settings != null ? settings.getMarketingOption()     : null
        );
    }

    // PUT /users/me/settings
    @Transactional
    public UserSettingsResponse updateSettings(Long userId, UpdateUserSettingsRequest req) {
        // 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        // 1) 알림/마케팅 업데이트 (null은 변경 안 함)
        UserSettings settings = userSettingsRepository.findByUserId(userId).orElseGet(() -> {
            UserSettings s = new UserSettings();
            s.setUser(user);
            return s;
        });

        if (req.getWarnOverBudget() != null)      settings.setNotifyBudgetAlert(req.getWarnOverBudget());
        if (req.getGifticonExpireAlert() != null) settings.setNotifyGifticonExpire(req.getGifticonExpireAlert());
        if (req.getEventAlert() != null)          settings.setNotifyBenefitEvents(req.getEventAlert());
        if (req.getAllowKakaoAlert() != null)     settings.setAllowKakaoAlert(req.getAllowKakaoAlert());
        if (req.getMarketingOptIn() != null)      settings.setMarketingOption(req.getMarketingOptIn());
        userSettingsRepository.save(settings);

        // 2) 선호 카테고리 업데이트 (이름 or ID. 둘 다 오면 "이름" 우선)
        List<String> names = new ArrayList<>();
        boolean hasNames = req.getPreferredCategories() != null && !req.getPreferredCategories().isEmpty();
        boolean hasIds   = req.getPreferredCategoryIds() != null && !req.getPreferredCategoryIds().isEmpty();

        if (hasNames || hasIds) {
            // 최대 3개 제한
            int count = hasNames ? req.getPreferredCategories().size() : req.getPreferredCategoryIds().size();
            if (count > 3) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "max 3 categories");

            // 기존 전체 삭제 후 재삽입
            userPreferredCategoryRepository.deleteAllByUserId(userId);

            if (hasNames) {
                // 이름으로 저장
                for (String n : req.getPreferredCategories()) {
                    Category c = categoryRepository.findByName(n)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid category: " + n));
                    UserPreferredCategory upc = new UserPreferredCategory();
                    upc.setUser(user);
                    upc.setCategory(c);
                    userPreferredCategoryRepository.save(upc);
                    names.add(c.getName());
                }
            } else {
                // ID로 저장
                for (Long cid : req.getPreferredCategoryIds()) {
                    Category c = categoryRepository.findById(cid)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid category id: " + cid));
                    UserPreferredCategory upc = new UserPreferredCategory();
                    upc.setUser(user);
                    upc.setCategory(c);
                    userPreferredCategoryRepository.save(upc);
                    names.add(c.getName());
                }
            }
        } else {
            // 카테고리를 안 보냈다면 기존 유지 → 현재 값 재조회
            names = userPreferredCategoryRepository.findCategoryNamesByUserId(userId);
        }

        return new UserSettingsResponse(
                names,
                settings.getNotifyBudgetAlert(),
                settings.getNotifyGifticonExpire(),
                settings.getNotifyBenefitEvents(),
                settings.getAllowKakaoAlert(),
                settings.getMarketingOption()
        );
    }
}