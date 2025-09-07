package com.berryselect.backend.auth.controller;

import com.berryselect.backend.auth.dto.response.UserProfileResponse;
import com.berryselect.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    // Authorization : Bearer <우리 JWT> 필요 (SecurityConfig에 의해 보호됨)
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication auth){
        Long userId = Long.valueOf(auth.getName()); // JwtProvider.setSubject = userId
        var user = userRepository.findById(userId).orElseThrow();

        return ResponseEntity.ok(
                UserProfileResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .phone(user.getPhone())
                        .birth(user.getBirth())
                        .build()
        );
    }
}
