package com.berryselect.backend.wallet.controller;

import com.berryselect.backend.wallet.domain.type.GifticonStatus;
import com.berryselect.backend.wallet.dto.request.GifticonCreateRequest;
import com.berryselect.backend.wallet.dto.request.MembershipCreateRequest;
import com.berryselect.backend.wallet.dto.response.*;
import com.berryselect.backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;  // Security 연동 전 임시
        return ResponseEntity.ok(walletService.getCardList(userId));
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<AssetResponse> getCard(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @PathVariable Long cardId
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        return ResponseEntity.ok(walletService.getCardDetail(userId, cardId));
    }

    /**
     * =====================
     * Membership
     * =====================
     */
    @GetMapping("/memberships")
    public ResponseEntity<MembershipSummaryResponse> listMemberships(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        return ResponseEntity.ok(walletService.getMembershipList(userId));
    }

    @GetMapping("/memberships/{membershipId}")
    public ResponseEntity<MembershipResponse> getMembership(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @PathVariable Long membershipId
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        return ResponseEntity.ok(walletService.getMembershipDetail(userId, membershipId));
    }

    @PostMapping("/memberships")
    public ResponseEntity<MembershipResponse> createMembership(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @RequestBody MembershipCreateRequest req
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        return ResponseEntity.ok(walletService.createMembership(userId, req));
    }

    @DeleteMapping("/memberships/{membershipId}")
    public ResponseEntity<Void> deleteMembership(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @PathVariable Long membershipId
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
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
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        return ResponseEntity.ok(walletService.getGifticonList(userId));
    }

    @GetMapping("/gifticons/{gifticonId}")
    public ResponseEntity<GifticonResponse> getGifticon(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @PathVariable Long gifticonId
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        return ResponseEntity.ok(walletService.getGifticonDetail(userId, gifticonId));
    }

    // 1. 기프티콘 등록 - 번호 등록
    @PostMapping(value = "/gifticons", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GifticonResponse> createGifticonJson(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @RequestBody GifticonCreateRequest req
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        return ResponseEntity.ok(walletService.createGifticon(userId, req));
    }

    // 2. 기프티콘 등록 - 이미지 등록 (이미지는 읽기만 하고 DB 저장 X)
    @PostMapping(value = "/gifticons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GifticonResponse> createGifticonMultipart(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @ModelAttribute GifticonCreateRequest req
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;

        if (req.getImage() != null && !req.getImage().isEmpty()) {
            try {
                byte[] bytes = req.getImage().getBytes();
                // TODO: 필요 시 OCR/바코드 인식 등 임시 처리 (DB 보관 X)
            } catch (Exception ignore) { /* 실패해도 등록은 진행 */ }
        }

        return ResponseEntity.ok(walletService.createGifticon(userId, req));
    }

    @PutMapping("/gifticons/{gifticonId}")
    public ResponseEntity<GifticonResponse> updateGifticonStatus(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @PathVariable Long gifticonId,
            @RequestParam("status") GifticonStatus gifticonStatus
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        return ResponseEntity.ok(walletService.updateGifticonStatus(userId, gifticonId, gifticonStatus));
    }

    @DeleteMapping("/gifticons/{gifticonId}")
    public ResponseEntity<Void> deleteGifticon(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @PathVariable Long gifticonId
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        walletService.deleteGifticon(userId, gifticonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/gifticons/{gifticonId}/redeem")
    public ResponseEntity<Void> redeemGifticon(
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader,
            @PathVariable Long gifticonId,
            @RequestParam Integer usedAmount
    ) {
        Long userId = (userIdHeader != null) ? userIdHeader : 1L;
        walletService.redeemGifticon(userId, gifticonId, usedAmount);
        return ResponseEntity.noContent().build();
    }
}