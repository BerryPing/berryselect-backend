package com.berryselect.backend.wallet.service;

import com.berryselect.backend.auth.dto.response.UserSettingsResponse;
import com.berryselect.backend.wallet.adapter.client.SettingsApiClient;
import com.berryselect.backend.wallet.domain.GifticonRedemption;
import com.berryselect.backend.wallet.domain.UserAsset;
import com.berryselect.backend.wallet.domain.type.AssetType;
import com.berryselect.backend.wallet.domain.type.GifticonStatus;
import com.berryselect.backend.wallet.dto.request.GifticonCreateRequest;
import com.berryselect.backend.wallet.dto.request.MembershipCreateRequest;
import com.berryselect.backend.wallet.dto.response.*;
import com.berryselect.backend.wallet.mapper.WalletMapper;
import com.berryselect.backend.wallet.repository.GifticonRedemptionRepository;
import com.berryselect.backend.wallet.repository.ProductRepository;
import com.berryselect.backend.wallet.repository.UserAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final UserAssetRepository userAssetRepository;
    private final ProductRepository productRepository;
    private final GifticonRedemptionRepository gifticonRedemptionRepository;
    private final SettingsApiClient settingsApiClient;
    private final BenefitAggregationService benefitAggregationService;

    /**
     * =====================
     * Card
     * =====================
     */
    @Transactional(readOnly = true)
    public WalletSummaryResponse getCardList(Long userId) {
        var items = userAssetRepository
                .findByUserIdAndAssetTypeOrderByIdDesc(userId, AssetType.CARD)
                .stream()
                .map(WalletMapper::toCardSummary)
                .toList();
        return new WalletSummaryResponse("CARD", items);
    }

    @Transactional(readOnly = true)
    public AssetResponse getCardDetail(Long userId, Long cardId) {
        UserAsset ua = userAssetRepository
                .findByIdAndUserIdAndAssetType(cardId, userId, AssetType.CARD)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        return WalletMapper.toCardDetail(ua);
    }

    @Transactional(readOnly = true)
    public CardBenefitsResponse getCardBenefits(Long userId, Long cardId) {
        UserAsset card = userAssetRepository
                .findByIdAndUserIdAndAssetType(cardId, userId, AssetType.CARD)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));

        // 1) 사용자 선호 카테고리 (이름 리스트)
        UserSettingsResponse settings = settingsApiClient.getUserSettings(userId).block();
        List<String> preferred = (settings == null || settings.getPreferredCategories() == null)
                ? List.of() : settings.getPreferredCategories();

        // 2) 룰 → 화면 아이템으로 변환(카테고리별)
        Map<String, List<CardBenefitsResponse.BenefitItem>> byCategory =
                benefitAggregationService.loadBenefitItemsGroupedByCategory(card);

        // 3) personalized / others 분리
        List<CardBenefitsResponse.BenefitGroup> personalized = new ArrayList<>();
        List<CardBenefitsResponse.BenefitGroup> others = new ArrayList<>();

        // 선호 카테고리 순서를 보존하며 push
        Set<String> preferredSet = new LinkedHashSet<>(preferred);
        for (String cat : preferredSet) {
            var items = byCategory.get(cat);
            if (items != null && !items.isEmpty()) {
                personalized.add(new CardBenefitsResponse.BenefitGroup(cat, items));
            }
        }
        // 나머지
        for (var entry : byCategory.entrySet()) {
            if (!preferredSet.contains(entry.getKey())) {
                others.add(new CardBenefitsResponse.BenefitGroup(entry.getKey(), entry.getValue()));
            }
        }

        return new CardBenefitsResponse(personalized, others);
    }

    /**
     * =====================
     * Membership
     * =====================
     */
    @Transactional(readOnly = true)
    public MembershipSummaryResponse getMembershipList(Long userId) {
        var items = userAssetRepository
                .findByUserIdAndAssetTypeOrderByIdDesc(userId, AssetType.MEMBERSHIP)
                .stream()
                .map(WalletMapper::toMembershipSummary)
                .toList();
        return new MembershipSummaryResponse("MEMBERSHIP", items);
    }

    @Transactional(readOnly = true)
    public MembershipResponse getMembershipDetail(Long userId, Long membershipId) {
        UserAsset ua = userAssetRepository
                .findByIdAndUserIdAndAssetType(membershipId, userId, AssetType.MEMBERSHIP)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found"));
        return WalletMapper.toMembershipDetail(ua);
    }

    @Transactional
    public MembershipResponse createMembership(Long userId, MembershipCreateRequest req) {
        UserAsset ua = new UserAsset();
        ua.setUserId(userId);
        ua.setProduct(
                productRepository.findById(req.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product"))
        );
        ua.setAssetType(AssetType.MEMBERSHIP);
        ua.setExternalNo(req.getExternalNo());
        ua.setLevel(req.getLevel());

        UserAsset saved = userAssetRepository.save(ua);
        return WalletMapper.toMembershipDetail(saved);
    }

    @Transactional
    public void deleteMembership(Long userId, Long membershipId) {
        UserAsset ua = userAssetRepository
                .findByIdAndUserIdAndAssetType(membershipId, userId, AssetType.MEMBERSHIP)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found"));
        userAssetRepository.delete(ua);
    }

    /**
     * =====================
     * Gifticon
     * =====================
     */
    @Transactional(readOnly = true)
    public GifticonSummaryResponse getGifticonList(
            Long userId,
            GifticonStatus status,
            Integer soonDays,
            int page, int size, String sort
    ) {
        if (page < 0) page = 0;
        if (size <= 0) size = 50;

        Sort.Order order = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        LocalDate from = null, to = null;
        if (soonDays != null && soonDays > 0) {
            LocalDate todayKst = LocalDate.now(ZoneId.of("Asia/Seoul"));
            from = todayKst;
            to = todayKst.plusDays(soonDays);
        }

        Page<UserAsset> pageResult = userAssetRepository.searchGifticons(userId, status, from, to, pageable);

        var items = pageResult.getContent().stream()
                .map(WalletMapper::toGifticonSummary)
                .toList();

        return new GifticonSummaryResponse("GIFTICON", items);
    }

    private Sort.Order parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return new Sort.Order(Sort.Direction.ASC, "expiresAt");
        }
        String[] p = sort.split(",");
        String property = p[0].trim();
        boolean desc = (p.length > 1 && "desc".equalsIgnoreCase(p[1].trim()));
        return new Sort.Order(desc ? Sort.Direction.DESC : Sort.Direction.ASC, property);
    }

    @Transactional(readOnly = true)
    public GifticonResponse getGifticonDetail(Long userId, Long gifticonId) {
        UserAsset ua = userAssetRepository
                .findByIdAndUserIdAndAssetType(gifticonId, userId, AssetType.GIFTICON)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gifticon not found"));
        return WalletMapper.toGifticonDetail(ua);
    }

    @Transactional
    public GifticonResponse createGifticon(Long userId, GifticonCreateRequest req) {
        UserAsset ua = new UserAsset();
        ua.setUserId(userId);
        ua.setProduct(
                productRepository.findById(req.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product"))
        );
        ua.setAssetType(AssetType.GIFTICON);
        ua.setBarcode(req.getBarcode());
        ua.setBalance(req.getBalance());
        ua.setExpiresAt(req.getExpiresAt());
        ua.setGifticonStatus(GifticonStatus.ACTIVE);

        UserAsset saved = userAssetRepository.save(ua);
        return WalletMapper.toGifticonDetail(saved);
    }

    @Transactional
    public GifticonResponse updateGifticonStatus(Long userId, Long gifticonId, GifticonStatus gifticonStatus) {
        UserAsset ua = userAssetRepository
                .findByIdAndUserIdAndAssetType(gifticonId, userId, AssetType.GIFTICON)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gifticon not found"));

        ua.setGifticonStatus(gifticonStatus);
        return WalletMapper.toGifticonDetail(ua);
    }

    @Transactional
    public void deleteGifticon(Long userId, Long gifticonId) {
        UserAsset ua = userAssetRepository
                .findByIdAndUserIdAndAssetType(gifticonId, userId, AssetType.GIFTICON)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gifticon not found"));
        userAssetRepository.delete(ua);
    }

    @Transactional
    public void redeemGifticon(Long userId, Long gifticonId, Integer usedAmount) {
        if (usedAmount == null || usedAmount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usedAmount must be > 0");
        }

        UserAsset ua = userAssetRepository
                .findWithLockByIdAndUserIdAndAssetType(gifticonId, userId, AssetType.GIFTICON)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gifticon not found"));

        if (ua.getBalance() == null || ua.getBalance() < usedAmount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        ua.setBalance(ua.getBalance() - usedAmount);
        if (ua.getBalance() == 0) {
            ua.setGifticonStatus(GifticonStatus.USED);
        }

        GifticonRedemption redemption = new GifticonRedemption();
        redemption.setAsset(ua);
        redemption.setUsedAmount(usedAmount);
        redemption.setRedeemedAt(Instant.now());

        gifticonRedemptionRepository.save(redemption);
    }
}