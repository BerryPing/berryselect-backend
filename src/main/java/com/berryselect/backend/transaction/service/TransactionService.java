package com.berryselect.backend.transaction.service;

import com.berryselect.backend.recommendation.domain.RecommendationOption;
import com.berryselect.backend.recommendation.domain.RecommendationSession;
import com.berryselect.backend.recommendation.repository.RecommendationOptionRepository;
import com.berryselect.backend.recommendation.repository.RecommendationSessionRepository;
import com.berryselect.backend.transaction.domain.AppliedBenefit;
import com.berryselect.backend.transaction.domain.Transaction;
import com.berryselect.backend.transaction.dto.request.TransactionRequest;
import com.berryselect.backend.transaction.dto.response.TransactionResponse;
import com.berryselect.backend.transaction.repository.AppliedBenefitRepository;
import com.berryselect.backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AppliedBenefitRepository appliedBenefitRepository;
    private final RecommendationSessionRepository sessionRepository;
    private final RecommendationOptionRepository optionRepository;

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
}