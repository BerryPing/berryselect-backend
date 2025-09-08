package com.berryselect.backend.budget.repository;

import com.berryselect.backend.budget.domain.MonthlyCategorySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyCategorySummaryRepository extends JpaRepository<MonthlyCategorySummary, Long> {
    /**
     * 사용자 월별 카테고리별 지출 요약 조회 (지출액 내림차순)
     * - 프론트: 카테고리별 지출 건수 차트/목록 표시
     * - amountSpent(지출액), txCount(거래건수) 포함
     */
    List<MonthlyCategorySummary> findByUserIdAndYearMonthOrderByAmountSpentDesc(Long userId, String yearMonth);

}
