package com.berryselect.backend.recommendation.repository;

import com.berryselect.backend.recommendation.domain.RecommendationSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationSessionRepository extends JpaRepository<RecommendationSession, Long> { }