package com.berryselect.backend.auth.service;

import com.berryselect.backend.auth.domain.RefreshToken;
import com.berryselect.backend.auth.domain.User;
import com.berryselect.backend.auth.dto.kakao.KakaoTokenResponse;
import com.berryselect.backend.auth.dto.kakao.KakaoUserResponse;
import com.berryselect.backend.auth.repository.RefreshTokenRepository;
import com.berryselect.backend.auth.repository.UserRepository;
import com.berryselect.backend.security.util.JwtProvider;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.berryselect.backend.auth.dto.response.AuthResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOauthClient kakaoClient;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OnboardingTokenStore onboardingTokenStore;

    @Value("${jwt.refresh-token-exp}")
    private long refreshTokenExpMs;

    /** 1) code로 카카오 토큰 교환 → 2) 유저 조회 → 3) 우리 DB upsert → 4) JWT(access, refresh) 발급,저장 */
    @Transactional
    public AuthResult loginWithAuthorizationCode(String code) {
        // 1) code → Kakao token
        KakaoTokenResponse token = kakaoClient.exchangeCodeForToken(code);

        // 2) Kakao access_token으로 유저 정보
        KakaoUserResponse me = kakaoClient.getUserMe(token.getAccessToken());
        Long kakaoId = me.getId();

        var byProvider = userRepository.findByProviderAndProviderUserId(User.Provider.KAKAO, String.valueOf(kakaoId));

        boolean isNewUser = byProvider.isEmpty();

        // 새로운 user인 경우 db에 저장하지 않음
        if(isNewUser){

            onboardingTokenStore.put(String.valueOf(kakaoId), token.getAccessToken(), 10*60*1000L);

            String guestJwt = jwtProvider.createTempAccessToken(
                    "guest:" + kakaoId,
                    List.of(new SimpleGrantedAuthority("ROLE_GUEST"))
            );

            return AuthResult.builder()
                    .accessToken(guestJwt)
                    .refreshToken(null)
                    .isNewUser(true)
                    .build();
        }

        // 기존 유저는 그대로 db업데이트 + 토큰 발급
        User user = byProvider.get();
        userRepository.save(user);

        String accessJwt = jwtProvider.createAccessToken(String.valueOf(user.getId()), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String refreshJwt = jwtProvider.createRefreshToken(String.valueOf(user.getId()));
        storeRefreshToken(user, refreshJwt);

        return AuthResult.builder()
                .accessToken(accessJwt)
                .refreshToken(refreshJwt)
                .isNewUser(false)
                .build();
    }

    private void storeRefreshToken(User user, String refreshJwt) {
        Instant now = Instant.now();
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .refreshToken(refreshJwt)
                .createdAt(now)
                .expiresAt(now.plusMillis(refreshTokenExpMs))
                .build();
        refreshTokenRepository.save(rt);
    }

    /** 토큰 재발급 **/
    @Transactional
    public TokenRefreshResponse rotateTokens(String refreshToken){
        RefreshToken rt = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "invalid refresh token"));

        Instant now = Instant.now();
        if (rt.getRevokedAt() != null) throw new ResponseStatusException(UNAUTHORIZED, "revoked refresh token");
        if (rt.getUsedAt() != null)    throw new ResponseStatusException(UNAUTHORIZED, "already used");
        if (rt.getExpiresAt().isBefore(now)) throw new ResponseStatusException(UNAUTHORIZED, "expired refresh token");

        // 기존 토큰은 사용처리, 새 토큰 발급/저장
        rt.setUsedAt(now);
        rt.setRevokedAt(now); // 재사용 방지
        refreshTokenRepository.save(rt);

        Long userId = rt.getUser().getId();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String newAccess = jwtProvider.createAccessToken(String.valueOf(userId), authorities);
        String newRefresh = jwtProvider.createRefreshToken(String.valueOf(userId));

        storeRefreshToken(rt.getUser(), newRefresh);
        return new TokenRefreshResponse(newAccess, newRefresh, "Bearer");
    }

    /**  단일 디바이스 로그아웃 **/
    @Transactional
    public void logoutByRefreshToken(String refreshToken) {
        refreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(rt -> {
            rt.setRevokedAt(Instant.now());
            rt.setUsedAt(Instant.now());
            refreshTokenRepository.save(rt);
        });
    }

    /** 전체 로그아웃 **/
    @Transactional
    public void logoutAllDevices(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    private static String safe(String v) { return v == null ? "" : v; }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenRefreshResponse{
        private String accessToken;
        private String refreshToken;
        private String tokenType;
    }
}
