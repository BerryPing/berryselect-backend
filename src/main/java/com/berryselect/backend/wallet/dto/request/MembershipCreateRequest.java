package com.berryselect.backend.wallet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipCreateRequest {
    private Long productId;
    private String externalNo; // 멤버십 번호
    private String level;      // 등급
}