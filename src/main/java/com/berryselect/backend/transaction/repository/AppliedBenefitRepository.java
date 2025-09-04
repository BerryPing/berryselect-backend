package com.berryselect.backend.transaction.repository;

import com.berryselect.backend.transaction.domain.AppliedBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppliedBenefitRepository extends JpaRepository<AppliedBenefit, Long> {
    /**
     * 거래 ID 목록에 대한 할인 혜택 조회
     * - 프론트: 최근 거래 목록에서 각 거래별 할인받은 금액 표시
     */
    List<AppliedBenefit> findByTxIdIn(List<String> txIds);

    /**
     * 사용자 월별 총 절감금액 계산
     * - 프론트: 요약 페이지 절감금액 표시
     */
    @Query(value = """
        SELECT COALESCE(SUM(ab.savedAmount), 0)
        FROM AppliedBenefit ab
        JOIN Transaction t ON ab.txId = t.txId
        WHERE t.userId = :userId
        AND DATE_FORMAT(t.txTime, '%Y-%m') = :yearMonth
        """)
    Long getTotalSavedByUserAndMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );
}
