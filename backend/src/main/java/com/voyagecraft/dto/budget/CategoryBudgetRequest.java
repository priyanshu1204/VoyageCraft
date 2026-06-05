package com.voyagecraft.dto.budget;

import com.voyagecraft.enums.ExpenseCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryBudgetRequest {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    @NotNull(message = "Allocated amount is required")
    private BigDecimal allocatedAmount;
}
