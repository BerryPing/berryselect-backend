package com.berryselect.backend.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {
    private Long id;
    private String type;        // "CARD"
    private String productName; // 카드 상품명
    private String issuer;      // 카드사
    private String last4;
    private Long thisMonthSpend;
}