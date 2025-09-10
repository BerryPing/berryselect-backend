package com.berryselect.backend.budget.service;

import com.berryselect.backend.budget.domain.MonthlyBudget;
import com.berryselect.backend.budget.dto.request.MonthlyBudgetUpsertRequest;
import com.berryselect.backend.budget.dto.response.MonthlyBudgetResponse;
import com.berryselect.backend.budget.repository.MonthlyBudgetRepository;
import com.berryselect.backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    public MonthlyBudgetResponse getMonthlyBudgetSummary(Long userId, String yearMonth){

        YearMonth ym;

        // yearMonth 파싱
        if(yearMonth == null || yearMonth.isBlank()){
            ym = YearMonth.now(KST);
        }
        else {
            try{
                ym = YearMonth.parse(yearMonth, YM_FMT);
            }
            catch(DateTimeParseException e){
                // 400(BAD_REQUEST)로 응답
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "yearMonth는 yyyy-MM 형식이어야 합니다."
                );
            }
        }

        // 월 범위 [이번달 1일 00:00, 다음달 1일 00:00]
        Instant start = ym.atDay(1).atStartOfDay(KST).toInstant();
        Instant end = ym.plusMonths(1).atDay(1).atStartOfDay(KST).toInstant();

        // 이번달 지출 합계 조회
        Long sum = transactionRepository.sumAmountByUserAndPeriod(userId, start, end);
        int spent = (sum == null) ? 0 : sum.intValue();

        var opt = monthlyBudgetRepository.findByUserIdAndYearMonth(userId, ym.toString());

        if(opt.isEmpty()){
            return MonthlyBudgetResponse.builder()
                    .yearMonth(ym.toString())
                    .amountTarget(0)
                    .amountSpent(spent)
                    .amountRemaining(0)
                    .exceeded(false)
                    .updatedAt(null)
                    .lastMonthSpent(null)
                    .exists(false)
                    .build();
        }

        MonthlyBudget mb = opt.get();
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
                .lastMonthSpent(null)
                .exists(true)
                .build();
    }

    @Transactional
    public MonthlyBudgetResponse upsertMonthlyBudget(Long userId, MonthlyBudgetUpsertRequest req){

        // ym파싱
        YearMonth ym = YearMonth.parse(req.getYearMonth(), YM_FMT);

        // upsert (있으면 갱신, 없으면 생성)
        MonthlyBudget mb = monthlyBudgetRepository
                .findByUserIdAndYearMonth(userId, ym.toString())
                .map(e -> e.updateTarget(req.getAmountTarget()))
                .orElseGet(()-> MonthlyBudget.create(userId, ym.toString(), req.getAmountTarget()));

        monthlyBudgetRepository.save(mb);

        return getMonthlyBudgetSummary(userId, ym.toString());
    }
}
