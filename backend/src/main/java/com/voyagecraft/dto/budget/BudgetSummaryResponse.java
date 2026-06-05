package com.voyagecraft.dto.budget;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BudgetSummaryResponse {
    private Long tripId;
    private String baseCurrency;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;
    private double overallPercentUsed;
    private int totalExpenses;
    private List<CategoryBudgetResponse> categoryBreakdown;
    private List<DailySpendResponse> dailySpending;
}

