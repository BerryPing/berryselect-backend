package com.berryselect.backend.merchant.repository;

import com.berryselect.backend.merchant.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 가맹점 브랜드 조회용
 * 기본 CRUD
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

}
