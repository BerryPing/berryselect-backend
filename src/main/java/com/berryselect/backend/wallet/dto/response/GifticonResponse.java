package com.berryselect.backend.wallet.dto.response;

import com.berryselect.backend.wallet.domain.type.GifticonStatus;

public record GifticonResponse(
        Long id,
        String type,  // "GIFTICON"
        String name,
        String barcode,
        Integer balance,
        String expiresAt,
        GifticonStatus gifticonStatus  // ACTIVE/USED/EXPIRED
) {
}
