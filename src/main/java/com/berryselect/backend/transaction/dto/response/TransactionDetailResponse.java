package com.berryselect.backend.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 거래 상세 정보 응답 DTO
 * - 프론트: 거래 목록 카드에 표시될 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailResponse {
    private Long txId; // 거래 ID
    private String merchantName; // 가맹점명 (merchants 테이블에서 조회)
    private String categoryName; // 카테고리명 (categories 테이블에서 조회)
    private String paymentCardName; // 결제 카드/자산명 (user_assets, products 테이블에서 조회)
    private Integer paidAmount; // 결제 금액
    private LocalDateTime txTime; // 거래 일시
    @Builder.Default
    private List<AppliedBenefitResponse> appliedBenefits = List.of(); // 적용된 혜택 목록 (할인 상세)
    private Integer totalSavedAmount; // 총 할인받은 금액
    private Boolean isFromRecommendation; // 추천 시스템 사용 여부
    private String paymentMethod; // 결제 수단
}
