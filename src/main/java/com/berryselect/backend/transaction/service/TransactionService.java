package com.berryselect.backend.transaction.service;

import com.berryselect.backend.merchant.domain.Brand;
import com.berryselect.backend.merchant.repository.BrandRepository;
import com.berryselect.backend.merchant.repository.CategoryRepository;
import com.berryselect.backend.merchant.repository.MerchantRepository;
import com.berryselect.backend.transaction.domain.AppliedBenefit;
import com.berryselect.backend.transaction.domain.Transaction;
import com.berryselect.backend.transaction.dto.response.AppliedBenefitResponse;
import com.berryselect.backend.transaction.dto.response.TransactionDetailResponse;
import com.berryselect.backend.transaction.mapper.TransactionMapper;
import com.berryselect.backend.transaction.repository.AppliedBenefitRepository;
import com.berryselect.backend.transaction.repository.TransactionRepository;
import com.berryselect.backend.wallet.repository.ProductRepository;
import com.berryselect.backend.wallet.repository.UserAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 거래 관련 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TransactionService {
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final AppliedBenefitRepository appliedBenefitRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final MerchantRepository merchantRepository;
    private final UserAssetRepository userAssetRepository;
    private final ProductRepository productRepository;

    /**
     * 사용자 거래 내역 조회
     * - 프론트: 거래 목록 카드 표시용
     *
     * @param userId 사용자 ID
     * @param yearMonth 조회 년월 (YYYY-MM, null이면 전체)
     * @param categoryId 카테고리 ID (null이면 전체)
     * @param pageable 페이징 정보
     * @return 거래 상세 응답 목록 (페이징)
     */
    public Page<TransactionDetailResponse> getUserTransactions(
            Long userId, String yearMonth, Long categoryId, Pageable pageable) {

        log.info("사용자 거래 내역 조회 - userId: {}, yearMonth: {}, categoryId: {}",
                userId, yearMonth, categoryId);

        // 거래 내역 조회
        Page<Transaction> transactions = transactionRepository
                .findUserTransactionWithFilters(userId, yearMonth, categoryId, pageable);

        // 거래 ID 목록 추출
        List<Long> txIds = transactions.getContent().stream()
                .map(Transaction::getTxId)
                .collect(Collectors.toList());

        // 거래별 적용된 혜택 조회
        Map<Long, List<AppliedBenefit>> benefitsMap = getBenefitsMapByTxIds(txIds);

        // 거래별 상세 정보 변환
        return transactions.map(transaction -> {
            List<AppliedBenefit> benefits = benefitsMap.getOrDefault(transaction.getTxId(), List.of());

            // 가맹점명, 카테고리명, 카드명 조회
            String merchantName = getMerchantName(transaction.getMerchantId());
            String categoryName = getCategoryName(transaction.getCategoryId());
            String paymentCardName = getPaymentName(transaction.getPaymentAssetId());

            // 혜택 응답 목록 생성
            List<AppliedBenefitResponse> benefitResponses = benefits.stream()
                    .map(this::convertToBenefitResponse)
                    .collect(Collectors.toList());

            // 총 할인금액 계산
            Integer totalSavedAmount = benefits.stream()
                    .mapToInt(AppliedBenefit::getSavedAmount)
                    .sum();

            return transactionMapper.toDetailResponse(
                    transaction, merchantName, categoryName, paymentCardName, benefitResponses, totalSavedAmount);
        });
    }

    /**
     * 월별 추천 사용률 계산
     *
     * @param userId 사용자 ID
     * @param yearMonth 조회 년월 (YYYY-MM)
     * @return 추천 사용률 (0.0 ~ 1.0)
     */
    public Double getRecommendationUsageRate(Long userId, String yearMonth) {
        log.info("추천 사용률 계산 - userId: {}, yearMonth: {}", userId, yearMonth);

        Long totalTransactions = transactionRepository.countTotalTransactionsByMonth(userId, yearMonth);
        Long recommendationUsedTransactions = transactionRepository
                .countRecommendationUsedTransactions(userId, yearMonth);

        if (totalTransactions == 0) {
            return 0.0;
        }

        return recommendationUsedTransactions.doubleValue() / totalTransactions.doubleValue();
    }

    /**
     * 월별 총 절약금액 조회
     *
     * @param userId 사용자 ID
     * @param yearMonth 조회 년월 (YYYY-MM)
     * @return 총 절약금액
     */
    public Long getTotalSavedAmount(Long userId, String yearMonth) {
        log.info("월별 총 절약금액 조회 - userId: {}, yearMonth: {}", userId, yearMonth);

        return appliedBenefitRepository.getTotalSavedByUserAndMonth(userId, yearMonth);
    }

    // ==== 내부 헬퍼 메서드 ====
    /**
     * 거래 ID 목록에 대한 혜택 맵 생성
     */
    private Map<Long, List<AppliedBenefit>> getBenefitsMapByTxIds(List<Long> txIds) {
        if (txIds.isEmpty()) {
            return Map.of();
        }

        List<AppliedBenefit> allBenefits = appliedBenefitRepository.findByTxIdIn(txIds);

        return allBenefits.stream()
                .collect(Collectors.groupingBy(AppliedBenefit::getTxId));
    }

    /**
     * AppliedBenefit를 AppliedBenefitResponse로 변환
     */
    private AppliedBenefitResponse convertToBenefitResponse(AppliedBenefit benefit) {
        String sourceName = getSourceName(benefit.getSourceRef(), benefit.getSourceType());
        String benefitDescription = generateBenefitDescription(benefit);

        return transactionMapper.toBenefitResponse(benefit, sourceName, benefitDescription);
    }

    /**
     * 가맹점명 조회
     */
    private String getMerchantName(Long merchantId) {
        if (merchantId == null) {
            return "알 수 없는 가맹점";
        }

        return merchantRepository.findById(merchantId)
             .map(merchant -> {
                 if (merchant.getBrandId() != null) {
                     String brandName = brandRepository.findById(merchant.getBrandId())
                         .map(Brand::getName)
                         .orElse("");
                     return brandName.isEmpty() ? merchant.getName() : brandName + " " + merchant.getName();
                 }
                 return merchant.getName();
             })
             .orElse("알 수 없는 가맹점");

        return "가맹점 #" + merchantId;
    }

    /**
     * 카테고리명 조회
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "기타";
        }

        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("기타");
    }

    /**
     * 결제 카드/자산명 조회
     */
    private String getPaymentCardName(Long paymentAssetId) {
        if (paymentAssetId == null) {
            return "현금";
        }

        return userAssetRepository.findById(paymentAssetId)
             .map(asset -> productRepository.findById(asset.getProductId())
                 .map(Product::getName)
                 .orElse("알 수 없는 카드"))
                 .orElse("현금");

        return "카드 #" + paymentAssetId;
    }

    /**
     * 혜택 소스명 조회
     */
    private String getSourceName(Long sourceRef, SourceType sourceType) {
        if (sourceRef == null) {
            return sourceType.getKoreanName();
        }

        // TODO: sourceType에 따른 실제 이름 조회 구현 필요
        // switch (sourceType) {
        //     case CARD, MEMBERSHIP -> userAssetRepository.findById(sourceRef)...
        //     case GIFTICON -> userAssetRepository.findById(sourceRef)...
        // }

        return sourceType.getKoreanName() + " #" + sourceRef;
    }

    /**
     * 혜택 설명 생성
     */
    private String generateBenefitDescription(AppliedBenefit benefit) {
        return String.format("%,d원 절약", benefit.getSavedAmount());
    }
}
