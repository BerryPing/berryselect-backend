package com.berryselect.backend.benefits.repository;

import com.berryselect.backend.benefits.domain.BenefitRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BenefitRuleRepository extends JpaRepository<BenefitRule, Long> {

    @Query("SELECT r FROM BenefitRule r WHERE r.sourceRefId = :productId AND r.isActive = true")
    List<BenefitRule> findActiveByProductId(Long productId);
}
