package com.berryselect.backend.transaction.service;

import com.berryselect.backend.merchant.domain.Category;
import com.berryselect.backend.merchant.repository.CategoryRepository;
import com.berryselect.backend.merchant.repository.MerchantRepository;
import com.berryselect.backend.transaction.domain.AppliedBenefit;
import com.berryselect.backend.transaction.domain.Transaction;
import com.berryselect.backend.transaction.domain.SourceType;
import com.berryselect.backend.transaction.dto.response.AppliedBenefitResponse;
import com.berryselect.backend.transaction.dto.response.TransactionDetailResponse;
import com.berryselect.backend.transaction.mapper.TransactionMapper;
import com.berryselect.backend.transaction.repository.AppliedBenefitRepository;
import com.berryselect.backend.transaction.repository.TransactionRepository;
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
    private final MerchantRepository merchantRepository;
    private final UserAssetRepository userAssetRepository;

    /**
     * 사용자 거래 내역 조회
     */
    public Page<TransactionDetailResponse> getUserTransactions(
            Long userId, String yearMonth, Long categoryId, Pageable pageable) {

        log.info("사용자 거래 내역 조회 - userId: {}, yearMonth: {}, categoryId: {}",
                userId, yearMonth, categoryId);

        Page<Transaction> transactions = transactionRepository
                .findUserTransactionsWithFilters(userId, yearMonth, categoryId, pageable);

        List<Long> txIds = transactions.getContent().stream()
                .map(Transaction::getTxId)
                .collect(Collectors.toList());

        Map<Long, List<AppliedBenefit>> benefitsMap = getBenefitsMapByTxIds(txIds);

        return transactions.map(transaction -> {
            List<AppliedBenefit> benefits = benefitsMap.getOrDefault(transaction.getTxId(), List.of());

            String merchantName = getMerchantName(transaction.getMerchantId());
            String categoryName = getCategoryName(transaction.getCategoryId());
            String paymentCardName = getPaymentCardName(transaction.getPaymentAssetId());

            List<AppliedBenefitResponse> benefitResponses = benefits.stream()
                    .map(this::convertToBenefitResponse)
                    .collect(Collectors.toList());

            Integer totalSavedAmount = benefits.stream()
                    .mapToInt(AppliedBenefit::getSavedAmount)
                    .sum();

            return transactionMapper.toDetailResponse(
                    transaction, merchantName, categoryName, paymentCardName,
                    benefitResponses, totalSavedAmount);
        });
    }

    /**
     * 월별 추천 사용률 계산
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
     */
    public Long getTotalSavedAmount(Long userId, String yearMonth) {
        log.info("월별 총 절약금액 조회 - userId: {}, yearMonth: {}", userId, yearMonth);

        return appliedBenefitRepository.getTotalSavedByUserAndMonth(userId, yearMonth);
    }

    // ===== 내부 헬퍼 메서드 =====

    private Map<Long, List<AppliedBenefit>> getBenefitsMapByTxIds(List<Long> txIds) {
        if (txIds.isEmpty()) {
            return Map.of();
        }

        List<AppliedBenefit> allBenefits = appliedBenefitRepository.findByTxIdIn(txIds);

        return allBenefits.stream()
                .collect(Collectors.groupingBy(AppliedBenefit::getTxId));
    }

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
                    if (merchant.getBrand() != null) {
                        return merchant.getBrand().getName() + " " + merchant.getName();
                    }
                    return merchant.getName();
                })
                .orElse("알 수 없는 가맹점");
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
                .map(asset -> {
                    // 팀원이 @EntityGraph로 product를 함께 조회하도록 설정
                    if (asset.getProduct() != null) {
                        return asset.getProduct().getName();
                    }
                    return "알 수 없는 카드";
                })
                .orElse("현금");
    }

    /**
     * 혜택 소스명 조회
     */
    private String getSourceName(Long sourceRef, SourceType sourceType) {
        if (sourceRef == null) {
            return sourceType.getKoreanName();
        }

        return userAssetRepository.findById(sourceRef)
                .map(asset -> {
                    if (asset.getProduct() != null) {
                        return asset.getProduct().getName();
                    }
                    return sourceType.getKoreanName();
                })
                .orElse(sourceType.getKoreanName());
    }

    private String generateBenefitDescription(AppliedBenefit benefit) {
        return String.format("%,d원 절약", benefit.getSavedAmount());
    }
}
