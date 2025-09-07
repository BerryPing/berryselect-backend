package com.berryselect.backend.benefits.repository;

import com.berryselect.backend.benefits.domain.UserBenefitCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBenefitCounterRepository extends JpaRepository<UserBenefitCounter, Long> {

    Optional<UserBenefitCounter> findByUserIdAndRuleRuleIdAndPeriodKey(Long userId, Long ruleId, String periodKey);
}