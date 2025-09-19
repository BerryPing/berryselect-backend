package com.berryselect.backend.recommendation.controller;

import com.berryselect.backend.recommendation.dto.request.RecommendationRequest;
import com.berryselect.backend.recommendation.dto.response.RecommendationResponse;
import com.berryselect.backend.recommendation.service.RecommendationService;
import com.berryselect.backend.security.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/sessions")
    public RecommendationResponse create(@RequestBody RecommendationRequest req,
                                         @AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return recommendationService.createSession(req, authUser.getId());
    }

    @GetMapping("/sessions/{sessionId}")
    public RecommendationResponse getDetail(@PathVariable Long sessionId,@AuthenticationPrincipal AuthUser authUser
                                            ) {
        if (authUser == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return recommendationService.getSessionDetail(sessionId, authUser.getId());
    }

    @PostMapping("/options/{optionId}/choose")
    public RecommendationResponse chooseOption(
            @PathVariable Long optionId,
            @RequestParam Long sessionId,
            @AuthenticationPrincipal AuthUser authUser

    ) {
        if (authUser == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return recommendationService.chooseOption(sessionId, optionId, authUser.getId());
    }
}
