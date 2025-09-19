package com.berryselect.backend.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletSummaryResponse {
    private String type; // "CARD"
    private List<CardSummary> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardSummary {
        private Long id;
        private String productName;
        private String issuer;
        private String last4;
        private Long thisMonthSpend;
    }
}