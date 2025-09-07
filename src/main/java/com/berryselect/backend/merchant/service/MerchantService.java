package com.berryselect.backend.merchant.service;

import com.berryselect.backend.merchant.domain.Merchant;
import com.berryselect.backend.merchant.dto.request.MerchantSearchRequest;
import com.berryselect.backend.merchant.dto.response.MerchantSearchResponse;
import com.berryselect.backend.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantSearchResponse searchMerchants(MerchantSearchRequest req,
                                                  BigDecimal userLat,
                                                  BigDecimal userLng) {

        String keyword = sanitizeKeyword(req.getKeyword());
        Long categoryId = req.getCategoryId();
        Long lastId = (req.getLastId() != null && req.getLastId() > 0) ? req.getLastId() : null;
        int limit = (req.getLimit() != null && req.getLimit() > 0) ? req.getLimit() : 20;

        Pageable pageable = PageRequest.of(0, limit + 1);

        List<Merchant> rows = merchantRepository.findMerchants(keyword, categoryId, lastId, pageable);

        boolean hasMore = rows.size() > limit;
        if (hasMore) {
            rows = rows.subList(0, limit);
        }

        boolean canComputeDistance = (userLat != null && userLng != null);

        List<MerchantSearchResponse.MerchantInfo> infos = rows.stream().map(m -> {
            Double distanceMeters = null;
            String distanceText = null;

            if (canComputeDistance && m.getLat() != null && m.getLng() != null) {
                distanceMeters = haversineMeters(
                        userLat.doubleValue(), userLng.doubleValue(),
                        m.getLat().doubleValue(), m.getLng().doubleValue()
                );
                distanceText = formatDistance(distanceMeters);
            }

            return MerchantSearchResponse.MerchantInfo.builder()
                    .id(m.getId())
                    .name(m.getName())
                    .brandId(m.getBrand() != null ? m.getBrand().getId() : null)
                    .categoryId(m.getCategory() != null ? m.getCategory().getId() : null)
                    .categoryName(m.getCategory() != null ? m.getCategory().getName() : null)
                    .address(m.getAddress())
                    .lat(m.getLat())
                    .lng(m.getLng())
                    .kakaoPlaceId(m.getKakaoPlaceId())
                    .distanceMeters(distanceMeters)
                    .distanceText(distanceText)
                    .build();
        }).collect(Collectors.toList());

        String sortBy = req.getSortBy() != null ? req.getSortBy() : "id";
        if ("distance".equalsIgnoreCase(sortBy)) {
            if (canComputeDistance) {
                infos.sort(Comparator.comparing(MerchantSearchResponse.MerchantInfo::getDistanceMeters,
                        Comparator.nullsLast(Double::compareTo)));
            } else {
                infos.sort(Comparator.comparing(MerchantSearchResponse.MerchantInfo::getName,
                        Comparator.nullsLast(String::compareToIgnoreCase)));
            }
        } else if ("name".equalsIgnoreCase(sortBy)) {
            infos.sort(Comparator.comparing(MerchantSearchResponse.MerchantInfo::getName,
                    Comparator.nullsLast(String::compareToIgnoreCase)));
        } else {
            infos.sort(Comparator.comparing(MerchantSearchResponse.MerchantInfo::getId));
        }

        if (req.getMaxDistanceKm() != null && canComputeDistance) {
            double maxMeters = req.getMaxDistanceKm() * 1000.0;
            infos = infos.stream()
                    .filter(i -> i.getDistanceMeters() != null && i.getDistanceMeters() <= maxMeters)
                    .collect(Collectors.toList());
        }

        Long nextLastId = infos.isEmpty() ? null : infos.get(infos.size() - 1).getId();

        return MerchantSearchResponse.builder()
                .merchants(infos)
                .hasMore(hasMore)
                .lastId(nextLastId)
                .build();
    }

    private String sanitizeKeyword(String kw) {
        if (kw == null) return null;
        String t = kw.trim();
        return t.isEmpty() ? null : t;
    }

    private static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371_000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static String formatDistance(double meters) {
        if (meters < 1000.0) {
            return String.format("%.0fm", meters);
        }
        double km = meters / 1000.0;
        return String.format("%.1fkm", Math.round(km * 10.0) / 10.0);
    }
}