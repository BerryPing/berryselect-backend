package com.berryselect.backend.wallet.dto.response;

public record AssetResponse (
    Long id,
    String type,  // "CARD"
    String productName,  // 카드 상품명
    String issuer,  // 카드사
    String last4,
    Long thisMonthSpend
) { }