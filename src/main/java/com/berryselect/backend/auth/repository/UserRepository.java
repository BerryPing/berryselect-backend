package com.berryselect.backend.auth.repository;

import com.berryselect.backend.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderUserId(User.Provider provider, String providerUserId);

    // 활성 사용자 조회 (최근 30일 내 로그인)
    @Query("SELECT u FROM User u WHERE u.updatedAt >= :thirtyDaysAgo")
    List<User> findActiveUsers(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

}
