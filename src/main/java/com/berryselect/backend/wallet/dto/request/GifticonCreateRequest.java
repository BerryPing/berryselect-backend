package com.berryselect.backend.wallet.dto.request;

public record GifticonCreateRequest(
        Long productId,  // 기프티콘 상품 ID
        String barcode,  // 바코드 값
        Integer balance,  // 잔액
        String expiresAt  // 만료일 (yyyy-MM-dd)
) {
}
