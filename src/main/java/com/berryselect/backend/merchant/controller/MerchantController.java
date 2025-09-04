package com.berryselect.backend.merchant.controller;

import com.berryselect.backend.common.dto.ApiResponse;
import com.berryselect.backend.merchant.dto.request.MerchantSearchRequest;
import com.berryselect.backend.merchant.dto.response.MerchantSearchResponse;
import com.berryselect.backend.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "가맹점 검색", description = "무한 스크롤 방식 가맹점 검색 API")
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping("/search")
    @Operation(
            summary = "가맹점 검색 (무한 스크롤)",
            description = "현재 위치 기반 무한 스크롤 방식으로 가맹점을 검색합니다"
    )
    public ApiResponse<MerchantSearchResponse> searchMerchants(
            @Parameter(description = "사용자 현재 위도", example = "37.5665")
            @RequestParam(required = false) BigDecimal userLat,

            @Parameter(description = "사용자 현재 경도", example = "126.9780")
            @RequestParam(required = false) BigDecimal userLng,

            @Parameter(description = "검색 키워드 (가맹점명, 브랜드명)", example = "스타벅스")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "카테고리 ID", example = "1")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "마지막으로 받은 가맹점 ID (첫 요청시 생략)", example = "100")
            @RequestParam(required = false) Long lastId,

            @Parameter(description = "한 번에 가져올 개수", example = "20")
            @RequestParam(defaultValue = "20") Integer limit,

            @Parameter(description = "최대 거리 (km)", example = "2.0")
            @RequestParam(required = false) Double maxDistanceKm,

            @Parameter(description = "정렬 기준 (name, distance, id)", example = "distance")
            @RequestParam(defaultValue = "name") String sortBy) {

        log.info("가맹점 검색 요청: keyword={}, categoryId={}, lastId={}, limit={}",
                keyword, categoryId, lastId, limit);

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
