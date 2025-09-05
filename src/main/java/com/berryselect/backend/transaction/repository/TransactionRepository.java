package com.berryselect.backend.transaction.repository;

import com.berryselect.backend.transaction.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 사용자별 거래 내역 조회 (페이징, 필터링)
     * - 프론트: 최근 거래 목록 카드 표시용
     * - 가맹점, 결제금액, 카테고리 정보 포함
      */
    @Query(value = """
        SELECT t FROM Transaction t
                WHERE t.userId = :userId
                AND (:yearMonth IS NULL OR DATE_FORMAT(t.txTime, '%Y-%m') = :yearMonth)
                AND (:categoryId IS NULL OR t.categoryId = :categoryId)
                ORDER BY t.txTime DESC
        """)
    Page<Transaction> findUserTransactionsWithFilters(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    /**
     * 추천 사용률 계산용 - 베리픽 결제 추천을 통해 결제한 거래 수
     * - 프론트: 요약 페이지 추천 사용률 표시
     */
    @Query(value = """
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.userId = :userId
        AND t.sessionId IS NOT NULL
        AND DATE_FORMAT(t.txTime, '%Y-%m') = :yearMonth
        """)
    Long countRecommendationUsedTransactions(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );

    /**
     * 추천 사용률 계산용 - 해당 월 전체 거래 수 (베리픽 결제 추천 없이 일반 거래도 포함)
     * - 프론트: 요약 페이지 추천 사용률 표시
     */
    @Query(value = """
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.userId = :userId
        AND DATE_FORMAT(t.txTime, '%Y-%m') = :yearMonth
        """)
    Long countTotalTransactionsByMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );
}
