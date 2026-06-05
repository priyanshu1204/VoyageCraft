package com.voyagecraft.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripAnalyticsResponse {

    // ── Trip Overview ──
    private Long tripId;
    private String tripTitle;
    private String currency;
    private int totalDays;

    // ── Budget vs Actual ──
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;
    private double budgetUsedPercent;
    private List<CategoryBudgetComparison> budgetVsActual;

    // ── Time Allocation ──
    private int totalActivityMinutes;
    private int totalTransitMinutes;
    private double transitPercent;
    private double activityPercent;
    private double freeTimePercent;
    private List<CategoryTimeAllocation> timeByCategory;

    // ── Transit vs Activity Ratio ──
    private String transitActivityRatio;
    private int totalTransportLegs;
    private int totalActivities;
    private double avgTransitMinutesPerDay;
    private double avgActivityMinutesPerDay;

    // ── Spending Insights ──
    private BigDecimal avgDailySpend;
    private BigDecimal highestSingleExpense;
    private String highestExpenseTitle;
    private String topSpendingCategory;
    private List<DailySpendTrend> dailySpendTrend;

    // ── Nested DTOs ──

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBudgetComparison {
        private String category;
        private String categoryLabel;
        private String emoji;
        private BigDecimal budgeted;
        private BigDecimal actual;
        private BigDecimal variance;
        private double percentUsed;
        private String status; // UNDER, ON_TRACK, OVER
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryTimeAllocation {
        private String category;
        private String categoryLabel;
        private String emoji;
        private int totalMinutes;
        private double percent;
        private int activityCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySpendTrend {
        private String date;
        private BigDecimal amount;
        private int expenseCount;
    }
}
