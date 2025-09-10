package com.berryselect.backend.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipResponse {
    private Long id;
    private String type;        // "MEMBERSHIP"

    @JsonProperty("name")
    private String productName;

    private String externalNo;  // 멤버십 번호
    private String level;       // 등급
}