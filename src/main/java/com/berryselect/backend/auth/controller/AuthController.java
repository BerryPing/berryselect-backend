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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final KakaoOauthProperties kakaoProps;

    // 1. 로그인 시작 : 카카오 동의창으로 리다이렉트
    @GetMapping ("/kakao/authorize")
    public void kakaoAuthorize(HttpServletResponse res) throws IOException {
        String scope = URLEncoder.encode(
                "profile_nickname phone_number birthyear birthday",
                StandardCharsets.UTF_8
        );

        String redirect = URLEncoder.encode(kakaoProps.getRedirectUri(), StandardCharsets.UTF_8);

        String url =
                "https://kauth.kakao.com/oauth/authorize"
                        + "?response_type=code"
                        + "&client_id=" + kakaoProps.getRestKey()
                        + "&redirect_uri=" + redirect
                        + "&scope=" + scope;

        res.sendRedirect(url);
    }

    // 2. 콜백 : code 수신 -> 토큰 교환 -> 우리 JWT 발급
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code){
        var r = authService.loginWithAuthorizationCode(code);
        return ResponseEntity.ok(r);
    }
}
