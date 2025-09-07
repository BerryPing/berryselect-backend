package com.berryselect.backend.auth.repository;

import com.berryselect.backend.auth.domain.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    // deleteById(userId) 사용
}