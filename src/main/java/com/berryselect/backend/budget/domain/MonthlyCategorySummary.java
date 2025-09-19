package com.berryselect.backend.budget.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_category_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyCategorySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "`year_month`", nullable = false, length = 7)
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

    // 거래 추가 메서드 (비즈니스 로직)
    public void addTransaction(Long spentAmount, Long savedAmount) {
        this.amountSpent += spentAmount;
        this.amountSaved += savedAmount;
        this.txCount += 1;
        // updatedAt은 @UpdateTimestamp로 자동 업데이트됨
    }
}
