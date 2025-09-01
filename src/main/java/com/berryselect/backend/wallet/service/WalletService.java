package com.berryselect.backend.wallet.service;

import com.berryselect.backend.wallet.domain.UserAsset;
import com.berryselect.backend.wallet.domain.type.AssetType;
import com.berryselect.backend.wallet.dto.request.MembershipCreateRequest;
import com.berryselect.backend.wallet.dto.response.AssetResponse;
import com.berryselect.backend.wallet.dto.response.MembershipResponse;
import com.berryselect.backend.wallet.dto.response.MembershipSummaryResponse;
import com.berryselect.backend.wallet.dto.response.WalletSummaryResponse;
import com.berryselect.backend.wallet.mapper.WalletMapper;
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
}