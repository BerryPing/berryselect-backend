package com.berryselect.backend.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipResponse {
    private Long id;
    private String type;        // "MEMBERSHIP"
    private String productName;
    private String externalNo;  // 멤버십 번호
    private String level;       // 등급
}