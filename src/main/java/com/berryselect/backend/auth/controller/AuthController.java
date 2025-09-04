package com.berryselect.backend.auth.controller;

import com.berryselect.backend.auth.service.AuthService;
import com.berryselect.backend.auth.service.AuthService.TokenRefreshResponse;
import com.berryselect.backend.config.KakaoOauthProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final KakaoOauthProperties kakaoProps;

    // 1. 로그인 시작 : 카카오 동의창으로 리다이렉트
    @GetMapping ("/kakao/authorize")
    public void kakaoAuthorize(HttpServletResponse res) throws IOException {
        String scope = kakaoProps.getScope();
        String redirect = URLEncoder.encode(kakaoProps.getRedirectUri(), StandardCharsets.UTF_8);

        String url =
                "https://kauth.kakao.com/oauth/authorize"
                        + "?response_type=code"
                        + "&client_id=" + kakaoProps.getRestKey()
                        + "&redirect_uri=" + redirect
                        + (scope != null && !scope.isBlank()
                ? "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8)
                        : "");

        res.sendRedirect(url);
    }

    // 2. 콜백 : code 수신 -> 토큰 교환 -> 우리 JWT 발급
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code){
        var r = authService.loginWithAuthorizationCode(code);
        return ResponseEntity.ok(r);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest req){
        authService.logoutByRefreshToken(req.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @Getter
    @NoArgsConstructor
    public static class RefreshRequest {
        private String refreshToken;
    }





}
