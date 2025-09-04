package com.berryselect.backend.budget.repository;

import com.berryselect.backend.budget.domain.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {
    /**
     * 사용자 월별 AI 분석 리포트 조회
     * - 프론트: AI 분석 리포트 섹션 표시
     * - OpenAI로 생성된 월별 소비 패턴 분석 결과
     */
    Optional<AnalysisReport> findByUserIdAndYearMonthAndReportType(Long userId, YearMonth yearMonth, String reportType);
}
