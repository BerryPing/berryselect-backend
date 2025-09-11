package com.berryselect.backend.wallet.controller;

import com.berryselect.backend.security.dto.AuthUser;
import com.berryselect.backend.wallet.domain.type.GifticonStatus;
import com.berryselect.backend.wallet.dto.request.GifticonCreateRequest;
import com.berryselect.backend.wallet.dto.request.MembershipCreateRequest;
import com.berryselect.backend.wallet.dto.response.*;
import com.berryselect.backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    /**
     * =====================
     * Card
     * =====================
     */
    @GetMapping("/cards")
    public ResponseEntity<WalletSummaryResponse> listCards(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        log.info("[WalletController] listCards userId={}", authUser != null ? authUser.getId() : null);
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.getCardList(userId));
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<AssetResponse> getCard(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long cardId
    ) {
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.getCardDetail(userId, cardId));
    }

    @GetMapping("/cards/{cardId}/benefits")
    public ResponseEntity<CardBenefitsResponse> getCardBenefits(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long cardId
    ) {
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.getCardBenefits(userId, cardId));
    }

    /**
     * =====================
     * Membership
     * =====================
     */
    @GetMapping("/memberships")
    public ResponseEntity<MembershipSummaryResponse> listMemberships(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.getMembershipList(userId));
    }

    @GetMapping("/memberships/{membershipId}")
    public ResponseEntity<MembershipResponse> getMembership(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long membershipId
    ) {
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.getMembershipDetail(userId, membershipId));
    }

    @PostMapping("/memberships")
    public ResponseEntity<MembershipResponse> createMembership(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody MembershipCreateRequest req
    ) {
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.createMembership(userId, req));
    }

    @DeleteMapping("/memberships/{membershipId}")
    public ResponseEntity<Void> deleteMembership(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long membershipId
    ) {
        Long userId = authUser.getId();
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
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "status", required = false) GifticonStatus status,
            @RequestParam(name = "soonDays", required = false) Integer soonDays,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "sort", defaultValue = "expiresAt,asc") String sort
    ) {
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.getGifticonList(userId, status, soonDays, page, size, sort));
    }

    @GetMapping("/gifticons/{gifticonId}")
    public ResponseEntity<GifticonResponse> getGifticon(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gifticonId
    ) {
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.getGifticonDetail(userId, gifticonId));
    }

    // 1. 기프티콘 등록 - 번호 등록
    @PostMapping(value = "/gifticons", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GifticonResponse> createGifticonJson(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody GifticonCreateRequest req
    ) {
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.createGifticon(userId, req));
    }

    // 2. 기프티콘 등록 - 이미지 등록 (이미지는 읽기만 하고 DB 저장 X)
    @PostMapping(value = "/gifticons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GifticonResponse> createGifticonMultipart(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute GifticonCreateRequest req
    ) {
        Long userId = authUser.getId();
        if (req.getImage() != null && !req.getImage().isEmpty()) {
            try {
                byte[] bytes = req.getImage().getBytes();
            } catch (Exception ignore) { /* 실패해도 등록은 진행 */ }
        }

        return ResponseEntity.ok(walletService.createGifticon(userId, req));
    }

    @PutMapping("/gifticons/{gifticonId}")
    public ResponseEntity<GifticonResponse> updateGifticonStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gifticonId,
            @RequestParam("status") GifticonStatus gifticonStatus
    ) {
        Long userId = authUser.getId();
        return ResponseEntity.ok(walletService.updateGifticonStatus(userId, gifticonId, gifticonStatus));
    }

    @DeleteMapping("/gifticons/{gifticonId}")
    public ResponseEntity<Void> deleteGifticon(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gifticonId
    ) {
        Long userId = authUser.getId();
        walletService.deleteGifticon(userId, gifticonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/gifticons/{gifticonId}/redeem")
    public ResponseEntity<Void> redeemGifticon(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gifticonId,
            @RequestParam Integer usedAmount
    ) {
        if (usedAmount == null || usedAmount <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Long userId = authUser.getId();
        walletService.redeemGifticon(userId, gifticonId, usedAmount);
        return ResponseEntity.noContent().build();
    }
}