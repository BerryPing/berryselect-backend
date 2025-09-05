package com.berryselect.backend.merchant.repository;

import com.berryselect.backend.merchant.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 조회용
 * 기본 CRUD
 * TransactionService에서 카테고리명 조회
 * ReportService에서 카테고리명 조회
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
