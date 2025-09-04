package com.berryselect.backend.budget.repository;

import com.berryselect.backend.budget.domain.MonthlyCategorySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyCategorySummaryRepository extends JpaRepository<MonthlyCategorySummary, Long> {
    /**
     * 사용자 월별 카테고리별 지출 요약 조회 (지출액 내림차순)
     * - 프론트: 카테고리별 지출 건수 차트/목록 표시
     * - amountSpent(지출액), txCount(거래건수) 포함
     */
    List<MonthlyCategorySummary> findByUserIdAndYearMonthOrderByAmountDesc(Long userId, String yearMonth);

    /**
     * 사용자 월별 전체 요약 통계 (총 지출, 총 절약, 총 거래건수)
     * - 프론트: 요약 페이지 상단 통계 수치
     * - 반환값: [총지출액, 총절약액, 총거래건수]
     */
    @Query(value = """
        SELECT
             COALESCE(SUM(mcs.amountSpent), 0),
             COALESCE(SUM(mcs.amountSaved), 0),
             COALESCE(SUM(mcs.txCount), 0)
        FROM MonthlyCategorySummary mcs
        WHERE mcs.userId = :userId
        AND mcs.yearMonth = :yearMonth
        """)
    Object[] getTotalSummaryByUserAndMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );
}
