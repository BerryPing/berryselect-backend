package com.berryselect.backend.merchant.repository;

import com.berryselect.backend.merchant.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    // 이름 목록으로 ID 목록 조회
    @Query("select c.id from Category c where c.name in :names")
    List<Long> findIdsByNames(@Param("names") List<String> names);
}