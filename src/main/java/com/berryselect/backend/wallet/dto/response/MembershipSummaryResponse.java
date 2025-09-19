package com.berryselect.backend.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipSummaryResponse {
    private String type = "MEMBERSHIP";
    private List<MembershipSummary> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipSummary {
        private Long id;

        @JsonProperty("name")
        private String productName;

        private String externalNo;
        private String level;
    }
}