package com.berryselect.backend.wallet.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final UserAssetRepository userAssetRepository;
    private final ProductRepository productRepository;
    private final GifticonRedemptionRepository gifticonRedemptionRepository;

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
                productRepository.findById(req.productId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product"))
        );
        ua.setAssetType(AssetType.MEMBERSHIP);
        ua.setExternalNo(req.externalNo());
        ua.setLevel(req.level());

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
    public GifticonSummaryResponse getGifticonList(Long userId) {
        var items = userAssetRepository
                .findByUserIdAndAssetTypeOrderByIdDesc(userId, AssetType.GIFTICON)
                .stream()
                .map(WalletMapper::toGifticonSummary)
                .toList();
        return new GifticonSummaryResponse("GIFTICON", items);
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
                productRepository.findById(req.productId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product"))
        );
        ua.setAssetType(AssetType.GIFTICON);
        ua.setBarcode(req.barcode());
        ua.setBalance(req.balance());
        ua.setExpiresAt(req.expiresAt() != null ? java.time.LocalDate.parse(req.expiresAt()) : null);
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
        UserAsset ua = userAssetRepository
                .findByIdAndUserIdAndAssetType(gifticonId, userId, AssetType.GIFTICON)
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
        redemption.setRedeemedAt(java.time.LocalDateTime.now());
        gifticonRedemptionRepository.save(redemption);
    }
}