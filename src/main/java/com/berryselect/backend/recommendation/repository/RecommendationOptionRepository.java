package com.berryselect.backend.recommendation.repository;

import com.berryselect.backend.recommendation.domain.RecommendationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationOptionRepository extends JpaRepository<RecommendationOption, Long> {

    @Query("select distinct o from RecommendationOption o " +
            "left join fetch o.items " +
            "where o.session.sessionId = :sessionId " +
            "order by o.rankOrder asc")
    List<RecommendationOption> findWithItemsBySessionId(@Param("sessionId") Long sessionId);
}