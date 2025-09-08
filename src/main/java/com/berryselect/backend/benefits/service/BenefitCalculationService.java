package com.berryselect.backend.benefits.service;

import com.berryselect.backend.benefits.domain.BenefitRule;
import com.berryselect.backend.benefits.domain.BenefitRuleLimit;
import com.berryselect.backend.benefits.domain.BenefitRuleScope;
import com.berryselect.backend.benefits.domain.UserBenefitCounter;
import com.berryselect.backend.benefits.repository.BenefitRuleRepository;
import com.berryselect.backend.benefits.repository.UserBenefitCounterRepository;
import com.berryselect.backend.wallet.domain.UserAsset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BenefitCalculationService {

    private final BenefitRuleRepository benefitRuleRepository;
    private final UserBenefitCounterRepository userBenefitCounterRepository;

    /**
     * 자산별 절감액 + 룰 상세 반환
     */
    public Map<UserAsset, List<RuleSaving>> calculatePerAssetRuleSave(
            List<UserAsset> combo, int amount, Long userId,
            Long merchantId, Long categoryId, Long brandId) {
        Map<UserAsset, List<RuleSaving>> saveMap = new HashMap<>();

        for (UserAsset ua : combo) {
            List<RuleSaving> ruleSavings = new ArrayList<>();

            List<BenefitRule> rules = benefitRuleRepository.findActiveByProductId(ua.getProduct().getId());
            for (BenefitRule rule : rules) {
                if (!isRuleApplicable(rule, merchantId, categoryId, brandId)) continue;

                int benefit = calcBaseBenefit(rule, amount);

                // 1회 최대 혜택 제한
                if (rule.getMaxBenefitAmount() != null && rule.getMaxBenefitAmount() > 0) {
                    benefit = Math.min(benefit, rule.getMaxBenefitAmount());
                }

                // 남은 한도
                int remaining = getRemainingLimit(rule, userId);
                if (remaining <= 0) continue;
                if (benefit > remaining) benefit = remaining;

                if (benefit > 0) {
                    ruleSavings.add(new RuleSaving(rule.getDescription(), benefit, rule.getRuleId()));
                }
            }
            saveMap.put(ua, ruleSavings);
        }
        return saveMap;
    }

    /** 총합 절감액 */
    public int calculateTotalSave(List<UserAsset> combo, int amount, Long userId,
                                  Long merchantId, Long categoryId, Long brandId) {
        return calculatePerAssetRuleSave(combo, amount, userId, merchantId, categoryId, brandId)
                .values().stream()
                .flatMap(List::stream)
                .mapToInt(RuleSaving::appliedValue)
                .sum();
    }

    /** RATE vs AMOUNT */
    private int calcBaseBenefit(BenefitRule rule, int amount) {
        // 최소 결제 금액 조건
        if (rule.getMinAmount() != null && amount < rule.getMinAmount()) {
            return 0;
        }

        int benefit = 0;

        if ("RATE".equals(rule.getValueType()) && rule.getValueRate() != null) {
            // 두 가지 경우: (1) 일반 % 할인, (2) 천원당 할인
            double rate = rule.getValueRate().doubleValue();

            if (rate < 1.0) {
                // ✅ 일반 퍼센트 할인 (예: 0.1 → 10%)
                benefit = (int) Math.floor(amount * rate);
            } else {
                // ✅ 천원 단위 step 할인 (예: 1000원당 100원 → value_rate=100)
                int step = 1000;
                int stepCount = amount / step;
                benefit = stepCount * (int) rate;
            }

        } else if ("AMOUNT".equals(rule.getValueType()) && rule.getValueAmount() != null) {
            // 정액 할인
            benefit = rule.getValueAmount();
        }

        // 1회 최대 할인 금액 적용
        if (rule.getMaxBenefitAmount() != null && rule.getMaxBenefitAmount() > 0) {
            benefit = Math.min(benefit, rule.getMaxBenefitAmount());
        }

        return Math.max(benefit, 0);
    }

    /** 스코프 검사 */
    private boolean isRuleApplicable(BenefitRule rule, Long merchantId, Long categoryId, Long brandId) {
        List<BenefitRuleScope> scopes = rule.getScopes();
        if (scopes == null || scopes.isEmpty()) return true;

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String today = now.getDayOfWeek().toString().substring(0, 3);
        int minutes = now.getHour() * 60 + now.getMinute();

        for (BenefitRuleScope s : scopes) {
            switch (s.getScopeType()) {
                case "DAY_OF_WEEK" -> {
                    if (s.getDayOfWeek() != null && s.getDayOfWeek().contains(today)) return true;
                }
                case "TIME" -> {
                    if (s.getStartMinute() != null && s.getEndMinute() != null &&
                            minutes >= s.getStartMinute() && minutes <= s.getEndMinute()) return true;
                }
                case "CATEGORY" -> {
                    if (Objects.equals(categoryId, s.getCategoryId())) return true;
                }
                case "BRAND" -> {
                    if (Objects.equals(brandId, s.getBrandId())) return true;
                }
                case "MERCHANT" -> {
                    if (Objects.equals(merchantId, s.getMerchantId())) return true;
                }

            }
        }
        return false;
    }

    /** 남은 한도 계산 */
    private int getRemainingLimit(BenefitRule rule, Long userId) {
        if (rule.getLimits() == null || rule.getLimits().isEmpty()) {
            return Integer.MAX_VALUE;
        }

        int remaining = Integer.MAX_VALUE;
        for (BenefitRuleLimit l : rule.getLimits()) {
            String key = makePeriodKey(l.getLimitType());
            var counterOpt = userBenefitCounterRepository.findByUserIdAndRuleRuleIdAndPeriodKey(userId, rule.getRuleId(), key);

            int used = counterOpt.map(UserBenefitCounter::getAmountUsed).orElse(0);
            int limit = l.getLimitAmount() != null ? l.getLimitAmount() : Integer.MAX_VALUE;
            remaining = Math.min(remaining, limit - used);
        }
        return Math.max(remaining, 0);
    }

    private String makePeriodKey(String type) {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        return switch (type) {
            case "MONTHLY" -> now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "DAILY" -> now.toString();
            default -> now.toString();
        };
    }



    /**
     * 절감액 + 설명을 담는 DTO
     */
    public record RuleSaving(String description, int appliedValue, Long ruleId) {}
}
