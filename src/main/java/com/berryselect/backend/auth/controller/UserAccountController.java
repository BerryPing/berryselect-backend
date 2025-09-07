package com.berryselect.backend.auth.controller;

import com.berryselect.backend.auth.service.UserAccountService;
import com.berryselect.backend.security.util.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserAccountController {

    private final JwtProvider jwtProvider;
    private final UserAccountService userAccountService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(HttpServletRequest request) {

        // 1) Authorization 검증
        String auth = request.getHeader("Authorization");
        if(auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String sub = jwtProvider.getSubject(auth.substring(7));
        if(sub == null || !sub.matches("\\d+")){
            return ResponseEntity.status(401).build();
        }

        Long userId = Long.parseLong(sub);

        userAccountService.deleteAccount(userId);

        return ResponseEntity.noContent().build();
    }
}
