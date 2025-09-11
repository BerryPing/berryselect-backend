package com.berryselect.backend.transaction.service;

import com.berryselect.backend.budget.domain.MonthlyCategorySummary;
import com.berryselect.backend.budget.repository.MonthlyCategorySummaryRepository;
import com.berryselect.backend.merchant.domain.Category;
import com.berryselect.backend.merchant.repository.CategoryRepository;
import com.berryselect.backend.merchant.repository.MerchantRepository;
import com.berryselect.backend.recommendation.domain.RecommendationOption;
import com.berryselect.backend.recommendation.domain.RecommendationSession;
import com.berryselect.backend.recommendation.repository.RecommendationOptionRepository;
import com.berryselect.backend.recommendation.repository.RecommendationSessionRepository;
import com.berryselect.backend.transaction.domain.AppliedBenefit;
import com.berryselect.backend.transaction.domain.Transaction;
import com.berryselect.backend.transaction.dto.request.TransactionRequest;
import com.berryselect.backend.transaction.dto.response.TransactionResponse;
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

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private final RecommendationSessionRepository sessionRepository;
    private final RecommendationOptionRepository optionRepository;
    private final MonthlyCategorySummaryRepository monthlyCategorySummaryRepository;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest req, Long userId) {
        RecommendationSession session = sessionRepository.findById(req.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid sessionId"));

        RecommendationOption option = optionRepository.findById(req.getOptionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid optionId"));

        if (!option.getSession().getSessionId().equals(session.getSessionId())) {
            throw new IllegalArgumentException("Option does not belong to this session");
        }

        // ✅ 거래 저장
        Transaction tx = Transaction.builder()
                .userId(userId)
                .merchantId(req.getMerchantId())
                .sessionId(session.getSessionId())
                .optionId(option.getOptionId())
                .paidAmount(req.getPaidAmount())
                .categoryId(req.getCategoryId())
                .txTime(Instant.now())
                .build();
        transactionRepository.save(tx);

        // ✅ 적용 혜택 저장
        List<AppliedBenefit> benefits = option.getItems().stream()
                .filter(i -> i.getRuleId() != null)
                .map(i -> AppliedBenefit.builder()
                        .tx(tx)
                        .ruleId(i.getRuleId())
                        .sourceType(i.getComponentType())
                        .sourceRef(i.getComponentRefId())
                        .savedAmount(i.getAppliedValue())
                        .build())
                .toList();
        appliedBenefitRepository.saveAll(benefits);

        // ✅ 월별 카테고리 요약 업데이트
        updateMonthlyCategorySummary(tx, benefits);


        return TransactionResponse.builder()
                .txId(tx.getTxId())
                .userId(userId)
                .merchantId(req.getMerchantId())
                .paidAmount(req.getPaidAmount())
                .txTime(tx.getTxTime())
                .appliedBenefits(
                        benefits.stream()
                                .map(b -> TransactionResponse.AppliedBenefitDto.builder()
                                        .ruleId(b.getRuleId())
                                        .sourceType(b.getSourceType())
                                        .sourceRef(b.getSourceRef())
                                        .savedAmount(b.getSavedAmount())
                                        .build())
                                .toList()
                )
                .build();
    }

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
        // 문자열 "YYYY-MM" → YearMonth
        YearMonth ym = YearMonth.parse(yearMonth);

        // Asia/Seoul 기준 해당 월 첫날 00:00과 다음 달 첫날 00:00
        ZonedDateTime startKst = ym.atDay(1).atStartOfDay(ZoneId.of("Asia/Seoul"));
        ZonedDateTime endKst   = ym.plusMonths(1).atDay(1).atStartOfDay(ZoneId.of("Asia/Seoul"));

        // UTC Instant로 변환 (DB에 저장된 tx_time은 UTC 기준)
        Instant startUtc = startKst.toInstant();
        Instant endUtc   = endKst.toInstant();

        // 수정한 Repository 메서드 호출
        Long total = transactionRepository.countTotalTransactionsByMonth(userId, startUtc, endUtc);
        Long used  = transactionRepository.countRecommendationUsedTransactions(userId, startUtc, endUtc);

        log.info("[추천 사용률 계산] userId={}, yearMonth={}, totalTxs={}, usedTxs={}, startUtc={}, endUtc={}",
                userId, yearMonth, total, used, startUtc, endUtc);

        if (total == 0) {
            return 0.0;
        }

        double rate = used.doubleValue() / total.doubleValue();
        log.info("[추천 사용률 결과] userId={}, yearMonth={}, rate={}", userId, yearMonth, rate);

        return used.doubleValue() / total.doubleValue();
    }

    /**
     * 월별 총 절약금액 조회
     */
    public Long getTotalSavedAmount(Long userId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        ZonedDateTime startKst = ym.atDay(1).atStartOfDay(ZoneId.of("Asia/Seoul"));
        ZonedDateTime endKst   = ym.plusMonths(1).atDay(1).atStartOfDay(ZoneId.of("Asia/Seoul"));
        Instant startUtc = startKst.toInstant();
        Instant endUtc   = endKst.toInstant();

        return appliedBenefitRepository.getTotalSavedByUserAndMonth(userId, startUtc, endUtc);
    }



    // ===== 내부 헬퍼 메서드 =====

    private Map<Long, List<AppliedBenefit>> getBenefitsMapByTxIds(List<Long> txIds) {
        if (txIds.isEmpty()) {
            return Map.of();
        }

        List<AppliedBenefit> allBenefits = appliedBenefitRepository.findByTx_TxIdIn(txIds);

        return allBenefits.stream()
                .collect(Collectors.groupingBy(ab -> ab.getTx().getTxId()));
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
    private String getSourceName(Long sourceRef, String sourceType) {
        String fallback = toKoreanSourceType(sourceType);
        if (sourceRef == null) {
            return fallback;
        }

        return userAssetRepository.findById(sourceRef)
                .map(asset -> {
                    if (asset.getProduct() != null) {
                        return asset.getProduct().getName();
                    }
                    return fallback;
                })
                .orElse(fallback);
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

    private String generateBenefitDescription(AppliedBenefit benefit) {
        return String.format("%,d원 절약", benefit.getSavedAmount());
    }

    /**
     * 월별 카테고리 요약 업데이트
     */
    private void updateMonthlyCategorySummary(Transaction transaction, List<AppliedBenefit> benefits) {
        if (transaction.getCategoryId() == null) {
            log.warn("거래에 카테고리가 없어 월별 요약 업데이트를 건너뜁니다. txId: {}", transaction.getTxId());
            return;
        }

        // 년월 계산 (KST 기준)
        String yearMonth = transaction.getTxTime()
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        // 총 절약 금액 계산
        Long totalSaved = benefits.stream()
                .mapToLong(AppliedBenefit::getSavedAmount)
                .sum();

        log.info("월별 카테고리 요약 업데이트 - userId: {}, yearMonth: {}, categoryId: {}, spent: {}, saved: {}",
                transaction.getUserId(), yearMonth, transaction.getCategoryId(),
                transaction.getPaidAmount(), totalSaved);

        // 기존 요약 조회 또는 새로 생성
        MonthlyCategorySummary summary = monthlyCategorySummaryRepository
                .findByUserIdAndYearMonthAndCategoryId(
                        transaction.getUserId(),
                        yearMonth,
                        transaction.getCategoryId())
                .orElse(MonthlyCategorySummary.builder()
                        .userId(transaction.getUserId())
                        .yearMonth(yearMonth)
                        .categoryId(transaction.getCategoryId())
                        .amountSpent(0L)
                        .amountSaved(0L)
                        .txCount(0)
                        .build());

        summary.addTransaction(transaction.getPaidAmount().longValue(), totalSaved);

        monthlyCategorySummaryRepository.save(summary);

        log.info("월별 카테고리 요약 업데이트 완료 - 총지출: {}, 총절약: {}, 거래건수: {}",
                summary.getAmountSpent(), summary.getAmountSaved(), summary.getTxCount());
    }
}



