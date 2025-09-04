package com.berryselect.backend.auth.controller;

import com.berryselect.backend.auth.service.AuthService;
import com.berryselect.backend.config.KakaoOauthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final KakaoOauthProperties kakaoProps;

    // 1. 로그인 시작 : 카카오 동의창으로 리다이렉트
    @GetMapping ("/kakao/authorize")
    public void kakaoAuthorize(HttpServletResponse res) throws IOException {
        String url = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoProps.getRestKey())
                .queryParam("redirect_uri", kakaoProps.getRedirectUri())
                .queryParam("scope", "profile_nickname account_email phone_number birthyear birthday")
                .build(true)
                .toUriString();
        res.sendRedirect(url);
    }

    // 2. 콜백 : code 수신 -> 토큰 교환 -> 우리 JWT 발급
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code){
        var r = authService.loginWithAuthorizationCode(code);
        return ResponseEntity.ok(r);
    }
}
