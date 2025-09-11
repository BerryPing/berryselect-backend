package com.berryselect.backend.recommendation.service;


import com.berryselect.backend.benefits.service.BenefitCalculationService;
import com.berryselect.backend.merchant.domain.Merchant;
import com.berryselect.backend.merchant.repository.MerchantRepository;
import com.berryselect.backend.recommendation.domain.RecommendationOption;
import com.berryselect.backend.recommendation.domain.RecommendationOptionItem;
import com.berryselect.backend.recommendation.domain.RecommendationSession;
import com.berryselect.backend.recommendation.dto.request.RecommendationRequest;
import com.berryselect.backend.recommendation.dto.response.RecommendationResponse;
import com.berryselect.backend.recommendation.repository.RecommendationOptionItemRepository;
import com.berryselect.backend.recommendation.repository.RecommendationOptionRepository;
import com.berryselect.backend.recommendation.repository.RecommendationSessionRepository;
import com.berryselect.backend.wallet.domain.UserAsset;
import com.berryselect.backend.wallet.repository.UserAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationSessionRepository sessionRepository;
    private final RecommendationOptionRepository optionRepository;
    private final RecommendationOptionItemRepository optionItemRepository;
    private final UserAssetRepository userAssetRepository;
    private final BenefitCalculationService benefitCalculationService;
    private final MerchantRepository merchantRepository;

    @Transactional
    public RecommendationResponse createSession(RecommendationRequest req, Long userId) {
        // ✅ merchant 조회해서 brand/category 자동 추출
        Merchant merchant = merchantRepository.findById(req.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid merchantId: " + req.getMerchantId()));

        Long brandId = merchant.getBrand() != null ? merchant.getBrand().getId() : null;
        Long categoryId = merchant.getCategory() != null ? merchant.getCategory().getId() : null;

        RecommendationSession session = RecommendationSession.builder()
                .userId(userId)
                .inputAmount(req.getAmount())
                .useGifticon(req.getUseGifticon())
                .createdAt(Instant.now())
                .build();
        sessionRepository.save(session);

        List<UserAsset> assets = userAssetRepository.findByUserId(userId);
        List<List<UserAsset>> combos = generateCombos(assets);

        List<RecommendationOption> options = new ArrayList<>();
        for (List<UserAsset> combo : combos) {
            Map<UserAsset, List<BenefitCalculationService.RuleSaving>> saveMap =
                    benefitCalculationService.calculatePerAssetRuleSave(
                            combo,
                            req.getAmount(),
                            userId,
                            req.getMerchantId(),
                            categoryId,
                            brandId
                    );

            int expectedSave = combo.stream()
                    .mapToInt(ua -> saveMap.getOrDefault(ua, List.of()).stream()
                            .max(Comparator.comparingInt(BenefitCalculationService.RuleSaving::appliedValue))
                            .map(BenefitCalculationService.RuleSaving::appliedValue)
                            .orElse(0))
                    .sum();
            int expectedPay = req.getAmount() - expectedSave;

            RecommendationOption option = RecommendationOption.builder()
                    .session(session)
                    .expectedPay(expectedPay)
                    .expectedSave(expectedSave)
                    .rankOrder((short) 0)
                    .build();
            optionRepository.save(option);

            short order = 1;
            for (UserAsset ua : combo) {
                List<BenefitCalculationService.RuleSaving> ruleSaves = saveMap.getOrDefault(ua, List.of());

                // 가장 절감액이 큰 rule 선택
                BenefitCalculationService.RuleSaving bestRule = ruleSaves.stream()
                        .max(Comparator.comparingInt(BenefitCalculationService.RuleSaving::appliedValue))
                        .orElse(null);

                if (bestRule != null) {
                    RecommendationOptionItem item = RecommendationOptionItem.builder()
                            .option(option)
                            .componentType(ua.getAssetType().name())
                            .componentRefId(ua.getId())
                            .ruleId(bestRule.ruleId())
                            .title(ua.getProduct().getName())
                            .appliedValue(bestRule.appliedValue())
                            .subtitle(bestRule.description() + " (절감 " + bestRule.appliedValue() + "원)")
                            .sortOrder(order++)
                            .build();
                    optionItemRepository.save(item);
                    option.getItems().add(item);
                }
            }
            options.add(option);
        }

        // ✅ 정렬 후 rankOrder 반영
        options.sort(Comparator.comparingInt(RecommendationOption::getExpectedSave).reversed());
        for (short i = 0; i < options.size(); i++) {
            options.get(i).setRankOrder((short) (i + 1));
        }

        session.setOptions(options); // ✅ 세션에 옵션 리스트 세팅

        return RecommendationResponse.fromEntity(session);
    }

    private List<List<UserAsset>> generateCombos(List<UserAsset> assets) {
        List<UserAsset> cards = assets.stream()
                .filter(a -> a.getAssetType().name().equals("CARD"))
                .toList();
        List<UserAsset> memberships = assets.stream()
                .filter(a -> a.getAssetType().name().equals("MEMBERSHIP"))
                .toList();

        List<List<UserAsset>> combos = new ArrayList<>();
        for (UserAsset card : cards) {
            combos.add(List.of(card));
            for (UserAsset m : memberships) {
                combos.add(List.of(card, m));
            }
        }
        return combos;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getSessionDetail(Long sessionId, Long userId) {
        RecommendationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sessionId: " + sessionId));

        // 사용자 소유 세션인지 검증
        if (!session.getUserId().equals(userId)) {
            throw new IllegalStateException("세션에 접근 권한이 없습니다.");
        }

        return RecommendationResponse.fromEntity(session);
    }

    @Transactional
    public RecommendationResponse chooseOption(Long sessionId, Long optionId, Long userId) {
        RecommendationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sessionId: " + sessionId));

        RecommendationOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid optionId: " + optionId));

        if (!option.getSession().getSessionId().equals(sessionId)) {
            throw new IllegalArgumentException("Option does not belong to this session");
        }

        if (!session.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인 세션이 아니므로 옵션을 선택할 수 없습니다.");
        }

        // ✅ 선택 확정
        session.setChosenOptionId(optionId);
        sessionRepository.save(session);

        return RecommendationResponse.fromEntity(session);
    }
}