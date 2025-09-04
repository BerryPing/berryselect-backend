package com.berryselect.backend.budget.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_category_summaries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyCategorySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "amount_spent")
    @Builder.Default
    private Long amountSpent = 0L;

    @Column(name = "amount_saved")
    @Builder.Default
    private Long amountSaved = 0L;

    @Column(name = "tx_count")
    @Builder.Default
    private Integer txCount = 0;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(3)")
    private LocalDateTime updatedAt;
}
