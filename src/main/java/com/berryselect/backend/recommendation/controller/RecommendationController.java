package com.berryselect.backend.recommendation.controller;

import com.berryselect.backend.recommendation.dto.request.RecommendationRequest;
import com.berryselect.backend.recommendation.dto.response.RecommendationResponse;
import com.berryselect.backend.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/sessions")
    public RecommendationResponse create(@RequestBody RecommendationRequest req,
                                         @RequestHeader("X-USER-ID") Long userId) {
        return recommendationService.createSession(req, userId);
    }

    @GetMapping("/sessions/{sessionId}")
    public RecommendationResponse getDetail(@PathVariable Long sessionId,
                                            @RequestHeader("X-USER-ID") Long userId) {
        return recommendationService.getSessionDetail(sessionId, userId);
    }

    @PostMapping("/options/{optionId}/choose")
    public RecommendationResponse chooseOption(
            @PathVariable Long optionId,
            @RequestParam Long sessionId
    ) {
        return recommendationService.chooseOption(sessionId, optionId);
    }
}
