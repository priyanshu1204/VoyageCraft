package com.voyagecraft.dto.budget;

import com.voyagecraft.enums.ExpenseCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CategoryBudgetResponse {
    private Long id;
    private ExpenseCategory category;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private double percentUsed;
}
