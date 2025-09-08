package com.berryselect.backend.transaction.mapper;

import com.berryselect.backend.transaction.domain.AppliedBenefit;
import com.berryselect.backend.transaction.domain.Transaction;
import com.berryselect.backend.transaction.dto.response.AppliedBenefitResponse;
import com.berryselect.backend.transaction.dto.response.TransactionDetailResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transaction 도메인 -> DTO 단순 변환 매퍼
 * - 필드 매핑만 담당
 * - 비즈니스 로직은 Service에서 처리
 */
@Component
public class TransactionMapper {
    /**
     * Transaction 엔티티를 TransactionDetailResponse로 변환
     * @param transaction 거래내역 엔티티
     * @param merchantName 가맹점명 (Service에서 조회하여 전달)
     * @param categoryName 카테고리명 (Service에서 조회하여 전달)
     * @param paymentCardName 결제카드명 (Service에서 조회하여 전달)
     * @param appliedBenefits 적용된 혜택 응답 목록 (Service에서 변환하여 전달)
     * @param totalSavedAmount 총 할인금액 (Service에서 계산하여 전달)
     * @return 거래 상세 응답 DTO
     */
    public TransactionDetailResponse toDetailResponse(
            Transaction transaction,
            String merchantName,
            String categoryName,
            String paymentCardName,
            List<AppliedBenefitResponse> appliedBenefits,
            Integer totalSavedAmount) {

        if(transaction == null) {
            return null;
        }

        return TransactionDetailResponse.builder()
                .txId(transaction.getTxId())
                .merchantName(merchantName != null ? merchantName : "알 수 없는 가맹점")
                .categoryName(categoryName != null ? categoryName : "기타")
                .paymentCardName(paymentCardName != null ? paymentCardName : "현금")
                .paidAmount(transaction.getPaidAmount())
                .txTime(transaction.getTxTime())
                .appliedBenefits(appliedBenefits != null ? appliedBenefits : List.of())
                .totalSavedAmount(totalSavedAmount != null ? totalSavedAmount : 0)
                .isFromRecommendation(transaction.getSessionId() != null)
                .paymentMethod(transaction.getPaymentMethod())
                .build();
    }

    /**
     * AppliedBenefit 엔티티를 AppliedBenefitResponse로 변환
     * @param appliedBenefit 적용된 혜택 엔티티
     * @param sourceName 혜택 소스명 (Service에서 조회하여 전달)
     * @param benefitDescription 혜택 설명 (Service에서 생성하여 전달)
     * @return 적용된 혜택 응답 DTO
     */
    public AppliedBenefitResponse toBenefitResponse(
            AppliedBenefit appliedBenefit,
            String sourceName,
            String benefitDescription) {

        if(appliedBenefit == null) {
            return null;
        }

        String sourceType = appliedBenefit.getSourceType();
        String sourceTypeKorean = toKoreanSourceType(sourceType);

        return AppliedBenefitResponse.builder()
                .id(appliedBenefit.getId())
                .sourceType(sourceType)
                .sourceName(sourceName != null ? sourceName : sourceTypeKorean)
                .savedAmount(appliedBenefit.getSavedAmount())
                .benefitDescription(benefitDescription != null ? benefitDescription :
                        String.format("%,d원 절약", appliedBenefit.getSavedAmount()))
                .sourceTypeKorean(sourceTypeKorean)
                .build();
    }

    /**
     * AppliedBenefit 목록을 AppliedBenefitResponse 목록으로 변환
     * @param appliedBenefits 적용된 혜택 엔티티 목록
     * @return 적용된 혜택 응답 DTO 목록
     */
    public List<AppliedBenefitResponse> toBenefitResponseList(List<AppliedBenefit> appliedBenefits) {
        if(appliedBenefits == null) {
            return List.of();
        }

        return appliedBenefits.stream()
                .map(benefit -> toBenefitResponse(benefit, null, null))
                .collect(Collectors.toList());
    }

    private String toKoreanSourceType(String sourceType) {
        if (sourceType == null) return "기타";
        return switch (sourceType) {
            case "CARD" -> "카드";
            case "MEMBERSHIP" -> "멤버십";
            case "GIFTICON" -> "기프티콘";
            default -> "기타";
        };
    }
}
