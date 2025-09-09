package com.berryselect.backend.budget.service;

import com.berryselect.backend.budget.domain.MonthlyBudget;
import com.berryselect.backend.budget.dto.request.MonthlyBudgetUpsertRequest;
import com.berryselect.backend.budget.dto.response.MonthlyBudgetResponse;
import com.berryselect.backend.budget.repository.MonthlyBudgetRepository;
import com.berryselect.backend.common.exception.ApiException;
import com.berryselect.backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    public MonthlyBudgetResponse getMonthlyBudgetSummary(Long userId, String yearMonthOrNull){

        // yearMonth 파싱
        YearMonth ym = (yearMonthOrNull == null || yearMonthOrNull.isBlank())
                ? YearMonth.now(KST)
                : YearMonth.parse(yearMonthOrNull.trim(), YM);

        // 해당 월 예산 엔티티 조회 (없으면 404형태의 예외)
        MonthlyBudget mb = monthlyBudgetRepository
                .findByUserIdAndYearMonth(userId, ym.toString())
                .orElseThrow(()-> new ApiException("해당 월의 예산이 설정되어 있지 않습니다."));

        // 월 범위 [이번달 1일 00:00, 다음달 1일 00:00]
        Instant start = ym.atDay(1).atStartOfDay(KST).toInstant();
        Instant end = ym.plusMonths(1).atDay(1).atStartOfDay(KST).toInstant();

        // 이번달 지출 합계 조회
        Long sum = transactionRepository.sumAmountByUserAndPeriod(userId, start, end);
        int spent = (sum == null) ? 0 : sum.intValue();

        // 남은 금액/초과 여부 계산
        int remaining = Math.max(0, mb.getAmountTarget() - spent);
        boolean exceeded = spent > mb.getAmountTarget();

        return MonthlyBudgetResponse.builder()
                .yearMonth(ym.toString())
                .amountTarget(mb.getAmountTarget())
                .amountSpent(spent)
                .amountRemaining(remaining)
                .exceeded(exceeded)
                .updatedAt(mb.getUpdatedAt())
                .build();
    }

    @Transactional
    public MonthlyBudgetResponse upsertMonthlyBudget(Long userId, MonthlyBudgetUpsertRequest req){

        // ym파싱
        YearMonth ym = YearMonth.parse(req.getYearMonth(), YM);

        // upsert (있으면 갱신, 없으면 생성)
        MonthlyBudget mb = monthlyBudgetRepository
                .findByUserIdAndYearMonth(userId, ym.toString())
                .map(e -> e.updateTarget(req.getAmountTarget()))
                .orElseGet(()-> MonthlyBudget.create(userId, ym.toString(), req.getAmountTarget()));

        monthlyBudgetRepository.save(mb);

        return getMonthlyBudgetSummary(userId, ym.toString());
    }
}
