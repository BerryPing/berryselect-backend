package com.berryselect.backend.budget.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBudgetResponse {
    private String yearMonth; //"yyyy-mm"
    private int amountTarget; // 목표금액
    private int amountSpent; // 이번달 지출 합계
    private int amountRemaining; // 남은 금액 ( total - spent )
    private boolean exceeded; // 초과 여부
    private LocalDateTime updatedAt; // 목표 갱신시각
    private Integer lastMonthSpent; // 지난달 지출 합계
    private boolean exists;
}
