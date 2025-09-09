package com.berryselect.backend.budget.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "monthly_budgets",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_year_month",
                columnNames = {"user_id", "year_month"}
        )
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "year_month", length = 7, nullable = false)
    private String yearMonth;

    @Column(name = "amount_target", nullable = false)
    private int amountTarget;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 새로운 목표 등록
    public static MonthlyBudget create(Long userId, String ym, int target){
        LocalDateTime now = LocalDateTime.now();
        return MonthlyBudget.builder()
                .userId(userId)
                .yearMonth(ym)
                .amountTarget(Math.max(0,target))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // 목표 금액 업데이트
    public MonthlyBudget updateTarget(int target){
        this.amountTarget = Math.max(0, target);
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    @PrePersist
    void onCreate(){
        LocalDateTime now = LocalDateTime.now();
        if(this.createdAt == null) this.createdAt = now;
    }

    @PreUpdate
    void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

}
