package com.berryselect.backend.transaction.dto.response;

import lombok.Data;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class TransactionResponse {
    private Long txId;
    private Long userId;
    private Long merchantId;
    private Integer paidAmount;
    private Instant txTime;
    private List<AppliedBenefitDto> appliedBenefits;

    @Data
    @Builder
    public static class AppliedBenefitDto {
        private Long ruleId;
        private String sourceType;
        private Long sourceRef;
        private Integer savedAmount;
    }
}
