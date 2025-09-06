package com.berryselect.backend.auth.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnboardingTokenStore {

    private static class Entry{
        final String kakaoAccessToken;
        final Instant expiresAt;
        Entry(String token, Instant expiresAt){
            this.kakaoAccessToken = token;
            this.expiresAt = expiresAt;
        }
        boolean expired(){
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, Entry> map = new ConcurrentHashMap<>();

    public void put(String kakaoId, String kakaoAccessToken, long ttlMillis){
        map.put(kakaoId, new Entry(kakaoAccessToken, Instant.now().plusMillis(ttlMillis)));
    }

    public String consume(String kakaoId){
        Entry e = map.remove(kakaoId);
        if(e == null || e.expired()) return null;
        return e.kakaoAccessToken;
    }
}
