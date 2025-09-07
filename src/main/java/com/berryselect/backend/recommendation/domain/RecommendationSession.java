package com.berryselect.backend.recommendation.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recommendation_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer inputAmount;

    @Column(nullable = false)
    private Boolean useGifticon = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecommendationOption> options = new ArrayList<>();

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }

    @Column(name = "chosen_option_id")
    private Long chosenOptionId;

}
