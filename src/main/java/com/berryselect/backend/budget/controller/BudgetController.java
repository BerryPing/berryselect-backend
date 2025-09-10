package com.berryselect.backend.budget.controller;

import com.berryselect.backend.budget.dto.request.MonthlyBudgetUpsertRequest;
import com.berryselect.backend.budget.dto.response.MonthlyBudgetResponse;
import com.berryselect.backend.budget.service.BudgetService;
import com.berryselect.backend.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/myberry/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // 목표 조회 -> 테스트 끝나면 사용할 코드
    @GetMapping
    public ApiResponse<MonthlyBudgetResponse> getMonthlyBudget(
            @RequestParam(required = false) String yearMonth,
            @AuthenticationPrincipal(expression = "id") Long userId
    ){
        return ApiResponse.success(budgetService.getMonthlyBudgetSummary(userId, yearMonth));
    }

//    //테스트용
//@GetMapping
//public ApiResponse<MonthlyBudgetResponse> getMonthlyBudget(
//        @RequestParam(required = false) String yearMonth,
//        Authentication authentication
//){
//    Long userId = 1L;
//    return ApiResponse.success(budgetService.getMonthlyBudgetSummary(userId, yearMonth));
//}



    // 목표 금액 설정 -> 테스트 끝나면 사용할 코드
    @PostMapping
    public ApiResponse<MonthlyBudgetResponse> upsertMonthlyBudget(
            @Valid @RequestBody MonthlyBudgetUpsertRequest req,
            @AuthenticationPrincipal(expression = "id") Long userId
    ){

        return ApiResponse.success(budgetService.upsertMonthlyBudget(userId, req));
    }

//    //테스트용
//    @PostMapping
//    public ApiResponse<MonthlyBudgetResponse> upsertMonthlyBudget(
//            @Valid @RequestBody MonthlyBudgetUpsertRequest req,
//            Authentication authentication
//    ){
//        // 임시: 인증 구현 전까지 하드코딩
//        Long userId = 1L;
//        return ApiResponse.success(budgetService.upsertMonthlyBudget(userId, req));
//    }
}
