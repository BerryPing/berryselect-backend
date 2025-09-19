package com.berryselect.backend.merchant.controller;

import com.berryselect.backend.common.dto.ApiResponse;
import com.berryselect.backend.merchant.dto.request.MerchantSearchRequest;
import com.berryselect.backend.merchant.dto.response.MerchantSearchResponse;
import com.berryselect.backend.merchant.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
@Slf4j
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping("/search")
    public ApiResponse<MerchantSearchResponse> searchMerchants(
            @RequestParam(required = false) BigDecimal userLat,

            @RequestParam(required = false) BigDecimal userLng,

            @RequestParam(required = false) String keyword,

            @RequestParam(required = false) Long categoryId,

            @RequestParam(required = false) Long lastId,

            @RequestParam(defaultValue = "20") Integer limit,

            @RequestParam(required = false) Double maxDistanceKm,

            @RequestParam(defaultValue = "name") String sortBy) {

        MerchantSearchRequest request = new MerchantSearchRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setLastId(lastId);
        request.setLimit(limit);
        request.setMaxDistanceKm(maxDistanceKm);
        request.setSortBy(sortBy);

        try {
            MerchantSearchResponse response = merchantService.searchMerchants(request, userLat, userLng);

            String message = String.format("%d개의 가맹점을 조회했습니다. %s",
                    response.getMerchants().size(),
                    response.getHasMore() ? "(더 보기 가능)" : "(마지막 페이지)");

            if ("distance".equalsIgnoreCase(sortBy) && (userLat == null || userLng == null)) {
                message += " (위치정보가 없어 이름 정렬로 대체)";
            }

            return ApiResponse.success(response, message);

        } catch (Exception e) {
            log.error("가맹점 검색 중 오류 발생", e);
            return ApiResponse.error("가맹점 검색 중 오류가 발생했습니다.");
        }
    }
}
