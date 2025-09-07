package com.berryselect.backend.auth.service;

import com.nimbusds.oauth2.sdk.token.Tokens;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnboardingTokenStore {

    private static class Entry{

        final String accessToken;
        final String refreshToken;
        final Instant expiresAt;

        Entry(String accessToken, String refreshToken, Instant expiresAt){
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresAt = expiresAt;
        }

        boolean expired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, Entry> map = new ConcurrentHashMap<>();

    // 저장
    public void put(String kakaoId, String accessToken, String refreshToken, long ttlMillis){
        map.put(kakaoId, new Entry(accessToken, refreshToken, Instant.now().plusMillis(ttlMillis)));
    }

    // 꺼내면서 삭제
    public Tokens consume(String kakaoId){
        Entry e = map.remove(kakaoId);
        if(e == null || e.expired()) return null;
        return new Tokens(e.accessToken, e.refreshToken, e.expiresAt);
    }

    // 꺼낸 후 사용할 DTO
    public static class Tokens{

        public final String accessToken;
        public final String refreshToken;
        public final Instant expiresAt;

        public Tokens(String accessToken, String refreshToken, Instant expiresAt){
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresAt = expiresAt;
        }
    }
}
