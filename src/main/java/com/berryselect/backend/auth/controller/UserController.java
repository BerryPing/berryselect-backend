package com.berryselect.backend.auth.controller;

import com.berryselect.backend.auth.dto.request.UpdateUserSettingsRequest;
import com.berryselect.backend.auth.dto.response.UserProfileResponse;
import com.berryselect.backend.auth.dto.response.UserSettingsResponse;
import com.berryselect.backend.auth.repository.UserRepository;
import com.berryselect.backend.auth.service.UserProfileService;
import com.berryselect.backend.auth.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserProfileService userProfileService;
    private final UserSettingsService userSettingsService;

    private Long requireUserId(String principal) {
        if (principal == null || principal.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "인증이 필요합니다.");
        }
        try {
            return Long.parseLong(principal);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(UNAUTHORIZED, "올바르지 않은 사용자 토큰입니다.");
        }
    }

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


    // GET /users/me/settings
    @GetMapping("/me/settings")
    public ResponseEntity<UserSettingsResponse> getSettings(
            @AuthenticationPrincipal String principal
    ) {
        Long userId = requireUserId(principal);
        return ResponseEntity.ok(userSettingsService.getSettings(userId));
    }

    // PUT /users/me/settings
    @PutMapping("/me/settings")
    public ResponseEntity<UserSettingsResponse> updateSettings(
            @AuthenticationPrincipal String principal,
            @RequestBody UpdateUserSettingsRequest req
    ) {
        Long userId = requireUserId(principal);
        return ResponseEntity.ok(userSettingsService.updateSettings(userId, req));
    }
}