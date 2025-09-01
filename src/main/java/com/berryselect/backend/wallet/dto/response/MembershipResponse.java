package com.berryselect.backend.wallet.dto.response;

public record MembershipResponse(
        Long id,
        String type,  // "MEMBERSHIP"
        String productName,
        String externalNo,  // 멤버십 번호
        String level  // 등급
) {
}
