package com.berryselect.backend.transaction.repository;

import com.berryselect.backend.transaction.domain.AppliedBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppliedBenefitRepository extends JpaRepository<AppliedBenefit, Long> {
    // saveAll(), findById(), deleteAll() 다 기본 제공됨
}