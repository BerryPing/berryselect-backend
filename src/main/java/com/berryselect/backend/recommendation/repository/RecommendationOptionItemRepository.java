package com.berryselect.backend.recommendation.repository;

import com.berryselect.backend.recommendation.domain.RecommendationOptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationOptionItemRepository extends JpaRepository<RecommendationOptionItem, Long> { }