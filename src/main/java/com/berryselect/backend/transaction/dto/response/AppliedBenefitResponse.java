package com.berryselect.backend.transaction.dto.response;

import com.berryselect.backend.transaction.domain.SourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 적용된 혜택 정보 응답 DTO
 * - 프론트: 거래별 할인 상세 표시
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppliedBenefitResponse {
    private Long id; // 혜택 ID
    private SourceType sourceType; // 혜택 소스 타입 (card, membership, gifticon)
    private String sourceName; // 혜택 소스명 (카드명, 멤버십명)
    private Integer savedAmount; // 할인받은 금액
    private String benefitDescription; // 혜택 설명 (ex: 5% 할인)
    private String sourceTypeKorean; // 혜택 소스 타입 한글명 (카드, 멤버십, 기프티콘)

}
