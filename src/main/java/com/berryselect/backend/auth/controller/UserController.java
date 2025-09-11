package com.berryselect.backend.auth.controller;

import com.berryselect.backend.auth.dto.request.UpdateUserSettingsRequest;
import com.berryselect.backend.auth.dto.response.UserProfileResponse;
import com.berryselect.backend.auth.dto.response.UserSettingsResponse;
import com.berryselect.backend.auth.repository.UserRepository;
import com.berryselect.backend.auth.service.UserProfileService;
import com.berryselect.backend.auth.service.UserSettingsService;
import com.berryselect.backend.security.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserProfileService userProfileService;
    private final UserSettingsService userSettingsService;

    // Authorization : Bearer <우리 JWT> 필요 (SecurityConfig에 의해 보호됨)
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
            @AuthenticationPrincipal AuthUser principal
    ){
        Long userId = principal.getId();
        var user = userRepository.findById(userId).orElseThrow();

        return ResponseEntity.ok(
                UserProfileResponse.builder()
                        .id(user.getId())
                        .name(user.getName() != null ? user.getName() : principal.getName())
                        .phone(user.getPhone())
                        .birth(user.getBirth())
                        .build()
        );
    }

    // GET /users/me/settings
    @GetMapping("/me/settings")
    public ResponseEntity<UserSettingsResponse> getSettings(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return ResponseEntity.ok(userSettingsService.getSettings(userId));
    }


    // PUT /users/me/settings
    @PutMapping("/me/settings")
    public ResponseEntity<UserSettingsResponse> updateSettings(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestBody UpdateUserSettingsRequest req
    ) {
        return ResponseEntity.ok(userSettingsService.updateSettings(userId, req));
    }

    // Security 연동 전
//    private Long resolveUserId(Authentication auth, Long headerUserId) {
//        if (auth != null && auth.getName() != null && auth.getName().matches("\\d+")) {
//            return Long.valueOf(auth.getName());
//        }
//        if (headerUserId != null) return headerUserId;
//        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not resolved");
//    }
}