package com.berryselect.backend.transaction.dto.request;

import lombok.Data;

@Data
public class TransactionRequest {
    private Long sessionId;     // 추천 세션 ID
    private Long optionId;      // 선택 옵션 ID
    private Long merchantId;    // 결제한 가맹점 ID
    private Integer paidAmount; // 실제 결제 금액
}
