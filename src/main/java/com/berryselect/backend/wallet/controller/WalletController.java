package com.berryselect.backend.wallet.controller;

import com.berryselect.backend.wallet.domain.type.GifticonStatus;
import com.berryselect.backend.wallet.dto.request.GifticonCreateRequest;
import com.berryselect.backend.wallet.dto.request.MembershipCreateRequest;
import com.berryselect.backend.wallet.dto.response.*;
import com.berryselect.backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    private Long toUserId(String principal) {
        return Long.parseLong(principal);
    }

    /**
     * =====================
     * Card
     * =====================
     */
    @GetMapping("/cards")
    public ResponseEntity<WalletSummaryResponse> listCards(
            @AuthenticationPrincipal String principal
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.getCardList(userId));
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<AssetResponse> getCard(
            @AuthenticationPrincipal String principal,
            @PathVariable Long cardId
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.getCardDetail(userId, cardId));
    }

    @GetMapping("/cards/{cardId}/benefits")
    public ResponseEntity<CardBenefitsResponse> getCardBenefits(
            @AuthenticationPrincipal String principal,
            @PathVariable Long cardId
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.getCardBenefits(userId, cardId));
    }

    /**
     * =====================
     * Membership
     * =====================
     */
    @GetMapping("/memberships")
    public ResponseEntity<MembershipSummaryResponse> listMemberships(
            @AuthenticationPrincipal String principal
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.getMembershipList(userId));
    }

    @GetMapping("/memberships/{membershipId}")
    public ResponseEntity<MembershipResponse> getMembership(
            @AuthenticationPrincipal String principal,
            @PathVariable Long membershipId
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.getMembershipDetail(userId, membershipId));
    }

    @PostMapping("/memberships")
    public ResponseEntity<MembershipResponse> createMembership(
            @AuthenticationPrincipal String principal,
            @RequestBody MembershipCreateRequest req
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.createMembership(userId, req));
    }

    @DeleteMapping("/memberships/{membershipId}")
    public ResponseEntity<Void> deleteMembership(
            @AuthenticationPrincipal String principal,
            @PathVariable Long membershipId
    ) {
        Long userId = toUserId(principal);
        walletService.deleteMembership(userId, membershipId);
        return ResponseEntity.noContent().build();
    }

    /**
     * =====================
     * Gifticon
     * =====================
     */
    @GetMapping("/gifticons")
    public ResponseEntity<GifticonSummaryResponse> listGifticons(
            @AuthenticationPrincipal String principal,
            @RequestParam(name = "status", required = false) GifticonStatus status,
            @RequestParam(name = "soonDays", required = false) Integer soonDays,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "sort", defaultValue = "expiresAt,asc") String sort
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.getGifticonList(userId, status, soonDays, page, size, sort));
    }

    @GetMapping("/gifticons/{gifticonId}")
    public ResponseEntity<GifticonResponse> getGifticon(
            @AuthenticationPrincipal String principal,
            @PathVariable Long gifticonId
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.getGifticonDetail(userId, gifticonId));
    }

    // 1. 기프티콘 등록 - 번호 등록
    @PostMapping(value = "/gifticons", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GifticonResponse> createGifticonJson(
            @AuthenticationPrincipal String principal,
            @RequestBody GifticonCreateRequest req
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.createGifticon(userId, req));
    }

    // 2. 기프티콘 등록 - 이미지 등록 (이미지는 읽기만 하고 DB 저장 X)
    @PostMapping(value = "/gifticons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GifticonResponse> createGifticonMultipart(
            @AuthenticationPrincipal String principal,
            @ModelAttribute GifticonCreateRequest req
    ) {
        Long userId = toUserId(principal);
        if (req.getImage() != null && !req.getImage().isEmpty()) {
            try {
                byte[] bytes = req.getImage().getBytes();
            } catch (Exception ignore) { /* 실패해도 등록은 진행 */ }
        }

        return ResponseEntity.ok(walletService.createGifticon(userId, req));
    }

    @PutMapping("/gifticons/{gifticonId}")
    public ResponseEntity<GifticonResponse> updateGifticonStatus(
            @AuthenticationPrincipal String principal,
            @PathVariable Long gifticonId,
            @RequestParam("status") GifticonStatus gifticonStatus
    ) {
        Long userId = toUserId(principal);
        return ResponseEntity.ok(walletService.updateGifticonStatus(userId, gifticonId, gifticonStatus));
    }

    @DeleteMapping("/gifticons/{gifticonId}")
    public ResponseEntity<Void> deleteGifticon(
            @AuthenticationPrincipal String principal,
            @PathVariable Long gifticonId
    ) {
        Long userId = toUserId(principal);
        walletService.deleteGifticon(userId, gifticonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/gifticons/{gifticonId}/redeem")
    public ResponseEntity<Void> redeemGifticon(
            @AuthenticationPrincipal String principal,
            @PathVariable Long gifticonId,
            @RequestParam Integer usedAmount
    ) {
        if (usedAmount == null || usedAmount <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Long userId = toUserId(principal);
        walletService.redeemGifticon(userId, gifticonId, usedAmount);
        return ResponseEntity.noContent().build();
    }
}