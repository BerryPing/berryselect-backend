package com.berryselect.backend.auth.repository;

import com.berryselect.backend.auth.domain.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    @Modifying
    @Query("delete from UserConsent uc where uc.user.id = :userId")
    void deleteAllByUserId(Long userId);

}