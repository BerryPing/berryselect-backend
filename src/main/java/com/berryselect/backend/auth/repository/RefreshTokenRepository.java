package com.berryselect.backend.auth.repository;

import com.berryselect.backend.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Modifying
    @Query("delete from RefreshToken rt where rt.expiresAt < :now")
    int deleteAllExpired(Instant now);

    @Modifying
    @Query("delete from RefreshToken rt where rt.user.id = :userId")
    int deleteAllByUserId(Long userId);

}
