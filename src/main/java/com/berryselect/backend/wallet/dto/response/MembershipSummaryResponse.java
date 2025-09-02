package com.berryselect.backend.wallet.dto.response;

import java.util.List;

public record MembershipSummaryResponse(
        String type,  // "MEMBERSHIP"
        List<MembershipSummary> items
) {
    public record MembershipSummary(
            Long id,
            String productName,
            String externalNo,
            String level
    ) { }
}
