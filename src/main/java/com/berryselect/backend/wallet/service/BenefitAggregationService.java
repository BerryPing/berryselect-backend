package com.berryselect.backend.wallet.service;

import com.berryselect.backend.benefits.domain.BenefitRule;
import com.berryselect.backend.benefits.domain.BenefitRuleScope;
import com.berryselect.backend.benefits.repository.BenefitRuleRepository;
import com.berryselect.backend.merchant.domain.Category;
import com.berryselect.backend.merchant.repository.CategoryRepository;
import com.berryselect.backend.wallet.domain.Brand;
import com.berryselect.backend.wallet.domain.UserAsset;
import com.berryselect.backend.wallet.dto.response.CardBenefitsResponse;
import com.berryselect.backend.wallet.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BenefitAggregationService {

    private final BenefitRuleRepository benefitRuleRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    /**
     * 카드(Product) 기준 활성 룰을 읽어 화면용 그룹(카테고리별)으로 변환
     */
    @Transactional(readOnly = true)
    public Map<String, List<CardBenefitsResponse.BenefitItem>> loadBenefitItemsGroupedByCategory(UserAsset card) {
        // 1) 룰 + 스코프 로드
        List<BenefitRule> rules = benefitRuleRepository.findActiveWithScopesByProductId(card.getProduct().getId());
        if (rules.isEmpty()) return Map.of();

        // 2) 스코프에서 참조된 카테고리/브랜드 이름 준비
        Set<Long> categoryIds = rules.stream()
                .flatMap(r -> r.getScopes().stream())
                .map(BenefitRuleScope::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> brandIds = rules.stream()
                .flatMap(r -> r.getScopes().stream())
                .map(BenefitRuleScope::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = categoryIds.isEmpty() ? Map.of()
                : categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        Map<Long, String> brandNameMap = brandIds.isEmpty() ? Map.of()
                : brandRepository.findAllById(brandIds).stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName));

        // 3) 카테고리명 → 혜택 아이템 리스트
        Map<String, List<CardBenefitsResponse.BenefitItem>> grouped = new LinkedHashMap<>();

        for (BenefitRule r : rules) {
            // 우선순위상 카테고리 스코프 우선(없으면 null)
            String categoryName = r.getScopes().stream()
                    .filter(s -> "CATEGORY".equalsIgnoreCase(s.getScopeType()) && s.getCategoryId() != null)
                    .map(s -> categoryNameMap.get(s.getCategoryId()))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("기타");

            // 브랜드명(있으면 사용, 없으면 카드 발급사/상품명 보조)
            String brandName = r.getScopes().stream()
                    .filter(s -> "BRAND".equalsIgnoreCase(s.getScopeType()) && s.getBrandId() != null)
                    .map(s -> brandNameMap.get(s.getBrandId()))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(Optional.ofNullable(card.getProduct().getBrandRef())
                            .map(Brand::getName)
                            .orElse(card.getProduct().getIssuer()));

            String title    = buildTitle(r);    // ex) "10% 할인", "3,000원 적립"
            String subtitle = buildSubtitle(r); // ex) "1회 최대 1만원 / 조건: 최소 2만원"

            CardBenefitsResponse.BenefitItem item =
                    new CardBenefitsResponse.BenefitItem(brandName, title, subtitle);

            grouped.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(item);
        }

        // 4) 각 카테고리 내부 정렬(룰 priority 우선)
        for (var e : grouped.entrySet()) {
            e.getValue().sort(Comparator.comparing(CardBenefitsResponse.BenefitItem::getTitle)); // 가벼운 정렬
        }

        return grouped;
    }

    private String buildTitle(BenefitRule r) {
        String typeKo = switch (upper(r.getBenefitType())) {
            case "DISCOUNT" -> "할인";
            case "CASHBACK" -> "캐시백";
            case "POINT" -> "적립";
            default -> "혜택";
        };
        String value = switch (upper(r.getValueType())) {
            case "RATE"   -> percent(r.getValueRate());
            case "AMOUNT" -> amount(r.getValueAmount());
            default -> "";
        };
        return value.isBlank() ? typeKo : (value + " " + typeKo);
    }

    private String buildSubtitle(BenefitRule r) {
        List<String> parts = new ArrayList<>();
        if (r.getMaxBenefitAmount() != null && r.getMaxBenefitAmount() > 0) {
            parts.add("1회 최대 " + amount(r.getMaxBenefitAmount()));
        }
        if (r.getMinAmount() != null && r.getMinAmount() > 0) {
            parts.add("최소 " + amount(r.getMinAmount()) + " 결제");
        }
        if (r.getDescription() != null && !r.getDescription().isBlank()) {
            parts.add(r.getDescription());
        }
        return String.join(" / ", parts);
    }

    private static String upper(String s) { return s == null ? "" : s.toUpperCase(Locale.ROOT); }

    private static String percent(BigDecimal rate) {
        if (rate == null) return "";
        // 0.1 → 10%
        return rate.scale() < 3 ? rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%"
                : rate.multiply(BigDecimal.valueOf(100)).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString() + "%";
    }

    private static String amount(Integer won) {
        if (won == null) return "";
        return String.format("%,d원", won);
    }
}