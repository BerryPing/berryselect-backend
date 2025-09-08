package com.berryselect.backend.merchant.repository;

import com.berryselect.backend.merchant.domain.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    @Query("""
        SELECT m
        FROM Merchant m
        LEFT JOIN FETCH m.category c
        LEFT JOIN FETCH m.brand b
        WHERE (:keyword IS NULL 
               OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(m.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR m.category.id = :categoryId)
          AND (:lastId IS NULL OR m.id > :lastId)
        ORDER BY m.id ASC
    """)
    List<Merchant> findMerchants(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("lastId") Long lastId,
            Pageable pageable
    );
}
