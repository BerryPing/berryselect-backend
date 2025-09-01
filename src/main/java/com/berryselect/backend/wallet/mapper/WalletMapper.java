package com.berryselect.backend.wallet.mapper;

import com.berryselect.backend.wallet.domain.UserAsset;
import com.berryselect.backend.wallet.dto.response.*;

public final class WalletMapper {
    private WalletMapper() {}

    /**
     * =====================
     * Card
     * =====================
     */
    public static WalletSummaryResponse.AssetSummary toCardSummary(UserAsset ua) {
        return new WalletSummaryResponse.AssetSummary(
                ua.getId(),
                ua.getProduct().getName(),
                ua.getProduct().getIssuer(),
                ua.getLast4(),
                ua.getThisMonthSpend()
        );
    }

    public static AssetResponse toCardDetail(UserAsset ua) {
        return new AssetResponse(
                ua.getId(),
                "CARD",
                ua.getProduct().getName(),
                ua.getProduct().getIssuer(),
                ua.getLast4(),
                ua.getThisMonthSpend()
        );
    }

    /**
     * =====================
     * Membership
     * =====================
     */
    public static MembershipSummaryResponse.MembershipSummary toMembershipSummary(UserAsset ua) {
        return new MembershipSummaryResponse.MembershipSummary(
                ua.getId(),
                ua.getProduct().getName(),
                ua.getExternalNo(),
                ua.getLevel()
        );
    }

    public static MembershipResponse toMembershipDetail(UserAsset ua) {
        return new MembershipResponse(
                ua.getId(),
                "MEMBERSHIP",
                ua.getProduct().getName(),
                ua.getExternalNo(),
                ua.getLevel()
        );
    }

    /**
     * =====================
     * Gifticon
     * =====================
     */
    public static GifticonSummaryResponse.GifticonSummary toGifticonSummary(UserAsset ua) {
        return new GifticonSummaryResponse.GifticonSummary(
                ua.getId(),
                ua.getProduct().getName(),
                ua.getBarcode(),
                ua.getBalance(),
                ua.getExpiresAt() != null ? ua.getExpiresAt().toString() : null,
                ua.getGifticonStatus()
        );
    }

    public static GifticonResponse toGifticonDetail(UserAsset ua) {
        return new GifticonResponse(
                ua.getId(),
                "GIFTICON",
                ua.getProduct().getName(),
                ua.getBarcode(),
                ua.getBalance(),
                ua.getExpiresAt() != null ? ua.getExpiresAt().toString() : null,
                ua.getGifticonStatus()
        );
    }
}