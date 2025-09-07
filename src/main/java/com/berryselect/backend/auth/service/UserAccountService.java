package com.berryselect.backend.auth.service;

import com.berryselect.backend.auth.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserPreferredCategoryRepository userPreferredCategoryRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void deleteAccount(Long userId){
        // 자식 먼저 정리
        userPreferredCategoryRepository.deleteAllByUserId(userId);
        userConsentRepository.deleteAllByUserId(userId);
        userSettingsRepository.deleteById(userId);
        refreshTokenRepository.deleteAllByUserId(userId);

        // 마지막으로 사용자 삭제
        userRepository.deleteById(userId);
    }


}
