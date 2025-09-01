package com.berryselect.backend.wallet.controller;

import com.berryselect.backend.wallet.dto.request.MembershipCreateRequest;
import com.berryselect.backend.wallet.dto.response.AssetResponse;
import com.berryselect.backend.wallet.dto.response.MembershipResponse;
import com.berryselect.backend.wallet.dto.response.MembershipSummaryResponse;
import com.berryselect.backend.wallet.dto.response.WalletSummaryResponse;
import com.berryselect.backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
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
}
