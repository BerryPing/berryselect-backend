package com.berryselect.backend.budget.repository;

import com.berryselect.backend.budget.domain.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    Optional<MonthlyBudget> findByUserIdAndYearMonth(Long userId, String yearMonth);
}
