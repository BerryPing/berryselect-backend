package com.berryselect.backend.transaction.repository;

import com.berryselect.backend.transaction.domain.AppliedBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AppliedBenefitRepository extends JpaRepository<AppliedBenefit, Long> {
    /**
     * 거래 ID 목록에 대한 할인 혜택 조회
     * - 프론트: 최근 거래 목록에서 각 거래별 할인받은 금액 표시
     */
    List<AppliedBenefit> findByTx_TxIdIn(List<Long> txIds);

    /**
     * 사용자 월별 총 절감금액 계산
     * - 프론트: 요약 페이지 절감금액 표시
     */
    @Query(value = """
    SELECT COALESCE(SUM(ab.saved_amount), 0)
    FROM applied_benefits ab
    JOIN transactions t ON ab.tx_id = t.tx_id
    WHERE t.user_id = :userId
      AND t.tx_time >= :startUtc
      AND t.tx_time < :endUtc
    """, nativeQuery = true)
    Long getTotalSavedByUserAndMonth(
            @Param("userId") Long userId,
            @Param("startUtc") Instant startUtc,
            @Param("endUtc") Instant endUtc
    );
}
