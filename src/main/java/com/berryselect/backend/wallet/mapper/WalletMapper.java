package com.berryselect.backend.wallet.mapper;

import com.berryselect.backend.wallet.domain.UserAsset;
import com.berryselect.backend.wallet.dto.response.*;
import com.berryselect.backend.wallet.dto.response.PersonalizedBenefitsResponse.BenefitCard;

import java.util.Collections;
import java.util.List;

public final class WalletMapper {
    private WalletMapper() {}

    /**
     * =====================
     * Card
     * =====================
     */
    public static WalletSummaryResponse.CardSummary toCardSummary(UserAsset ua) {
        return new WalletSummaryResponse.CardSummary(
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

    public static List<CardBenefitsResponse.BenefitGroup> filterBenefits(
            UserAsset card, List<String> preferredCategories
    ) {
        // TODO : benefits 연동 전
        return Collections.emptyList();
    }

    public static List<CardBenefitsResponse.BenefitGroup> filterOtherBenefits(
            UserAsset card, List<String> preferredCategories
    ) {
        // TODO : benefits 연동 전
        return Collections.emptyList();
    }

    // UserAsset의 "혜택 한 건"을 공통 BenefitCard로 변환
    public static BenefitCard toBenefitCard(UserAsset ua, Object rawBenefit) {
        String methodType  = ua.getAssetType().name();     // CARD | MEMBERSHIP | GIFTICON ...
        String methodName  = ua.getProduct().getName();    // 결제수단/멤버십 표시명
        String brandName   = extractBrandName(rawBenefit); // 예: "GS25"
        String title       = extractTitle(rawBenefit);     // 예: "편의점 10% 할인"
        String summary     = extractSummary(rawBenefit);   // 예: "월 최대 1만원 / 실적 30만원"
        String badge       = extractBadge(rawBenefit);     // 예: "쿠폰"

        return BenefitCard.builder()
                .assetId(ua.getId())
                .methodType(methodType)
                .methodName(methodName)
                .brandName(brandName)
                .title(title)
                .summary(summary)
                .badge(badge)
                .build();
    }

    // ====== 아래 4개는 프로젝트의 혜택 VO/DTO 구조에 맞게 구현하세요. (임시 스텁) ======
    private static String extractBrandName(Object b) { return getSafe(b, "brand", "brandName", "merchant"); }
    private static String extractTitle(Object b)     { return getSafe(b, "title", "name"); }
    private static String extractSummary(Object b)   { return getSafe(b, "summary", "desc", "description"); }
    private static String extractBadge(Object b)     { return getSafe(b, "badge", "type", "tag"); }

    private static String getSafe(Object obj, String... candidates) {
        if (obj == null) return null;
        for (String name : candidates) {
            try {
                var f = obj.getClass().getDeclaredField(name);
                f.setAccessible(true);
                Object v = f.get(obj);
                if (v != null) return String.valueOf(v);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }
        return null;
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