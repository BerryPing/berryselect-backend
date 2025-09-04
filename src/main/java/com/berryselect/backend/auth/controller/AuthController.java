package com.berryselect.backend.auth.controller;

import com.berryselect.backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code){
        var r = authService.loginWithAuthorizationCode(code);
        return ResponseEntity.ok(r);
    }
}
