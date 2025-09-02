package com.berryselect.backend.wallet.dto.response;

import com.berryselect.backend.wallet.domain.type.GifticonStatus;

import java.util.List;

public record GifticonSummaryResponse(
        String type,
        List<GifticonSummary> items
) {
    public record GifticonSummary(
            Long id,
            String name,
            String barcode,
            Integer balance,
            String expiresAt,
            GifticonStatus gifticonStatus  // ACTIVE/USED/EXPIRED
    ) {}
}
