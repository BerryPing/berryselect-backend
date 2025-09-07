package com.berryselect.backend.auth.repository;

import com.berryselect.backend.auth.domain.UserPreferredCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserPreferredCategoryRepository extends JpaRepository<UserPreferredCategory, Long> {

    @Modifying
    @Query("delete from UserPreferredCategory upc where upc.user.id = :userId")
    void deleteAllByUserId(Long userId);
}
