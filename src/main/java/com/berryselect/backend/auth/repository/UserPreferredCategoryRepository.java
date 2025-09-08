package com.berryselect.backend.auth.repository;

import com.berryselect.backend.auth.domain.UserPreferredCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserPreferredCategoryRepository extends JpaRepository<UserPreferredCategory, Long> {

    @Modifying
    @Query("delete from UserPreferredCategory upc where upc.user.id = :userId")
    void deleteAllByUserId(Long userId);

    // userId 기준으로 카테고리 이름 조회
    @Query("""
      select c.name
      from UserPreferredCategory upc
      join upc.category c
      where upc.user.id = :userId
      order by upc.id asc
    """)
    List<String> findCategoryNamesByUserId(Long userId);
}