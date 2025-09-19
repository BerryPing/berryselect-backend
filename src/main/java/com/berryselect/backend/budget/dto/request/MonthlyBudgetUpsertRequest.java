package com.berryselect.backend.budget.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBudgetUpsertRequest {

    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "yearMonth는 yyyy-MM 형식이어야 합니다.")
    private String yearMonth;

    @Min(value = 0, message = "amountTarget는 0 이상이어야 합니다.")
    private int amountTarget;
}
