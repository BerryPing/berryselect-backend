package com.berryselect.backend.wallet.dto.response;

import java.util.List;

public record WalletSummaryResponse (
        String type,  // "CARD"
        List<AssetSummary> items
) {
    public record AssetSummary(
            Long id,
            String productName,
            String issuer,
            String last4,
            Long thisMonthSpend
    ) { }
}
