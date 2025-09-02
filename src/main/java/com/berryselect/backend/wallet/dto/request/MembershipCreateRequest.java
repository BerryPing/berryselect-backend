package com.berryselect.backend.wallet.dto.request;

public record MembershipCreateRequest(
        Long productId,
        String externalNo,  // 멤버십 번호
        String level  // 등급
) {
}
