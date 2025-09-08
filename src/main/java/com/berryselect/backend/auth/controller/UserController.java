package com.berryselect.backend.auth.controller;

import com.berryselect.backend.auth.dto.request.UpdateUserSettingsRequest;
import com.berryselect.backend.auth.dto.response.UserProfileResponse;
import com.berryselect.backend.auth.dto.response.UserSettingsResponse;
import com.berryselect.backend.auth.repository.UserRepository;
import com.berryselect.backend.auth.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserProfileService userProfileService;

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
            Authentication auth,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId  // Security 연동 전

            // @AuthenticationPrincipal SecurityPrincipal principal  // Security 연동 후
    ) {
        Long userId = resolveUserId(auth, headerUserId);  // Security 연동 전
        // Long userId = principal.getId();   // Security 연동 후

        return ResponseEntity.ok(userProfileService.getSettings(userId));
    }


    // PUT /users/me/settings
    @PutMapping("/me/settings")
    public ResponseEntity<UserSettingsResponse> updateSettings(
            Authentication auth,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,  // Security 연동 전
            @RequestBody UpdateUserSettingsRequest req

            // @AuthenticationPrincipal SecurityPrincipal principal,  // Security 연동 후
            // @RequestBody UpdateUserSettingsRequest req
    ) {

        Long userId = resolveUserId(auth, headerUserId);  // Security 연동 전
        // Long userId = principal.getId();  // Security 연동 후

        return ResponseEntity.ok(userProfileService.updateSettings(userId, req));
    }

    // Security 연동 전
    private Long resolveUserId(Authentication auth, Long headerUserId) {
        if (auth != null && auth.getName() != null && auth.getName().matches("\\d+")) {
            return Long.valueOf(auth.getName());
        }
        if (headerUserId != null) return headerUserId;
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not resolved");
    }
}
