package com.voyagecraft.service;

import com.voyagecraft.dto.analytics.TripAnalyticsResponse;
import com.voyagecraft.dto.analytics.TripAnalyticsResponse.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.ExpenseCategory;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final ActivityRepository activityRepository;
    private final TransportDetailRepository transportDetailRepository;

    private static final Map<String, String[]> CAT_META = Map.ofEntries(
            Map.entry("TRANSPORT",     new String[]{"Transport",     "✈️"}),
            Map.entry("STAY",          new String[]{"Stay",          "🏨"}),
            Map.entry("FOOD",          new String[]{"Food",          "🍽️"}),
            Map.entry("ACTIVITIES",    new String[]{"Activities",    "🎯"}),
            Map.entry("SHOPPING",      new String[]{"Shopping",      "🛍️"}),
            Map.entry("ENTERTAINMENT", new String[]{"Entertainment", "🎭"}),
            Map.entry("INSURANCE",     new String[]{"Insurance",     "🛡️"}),
            Map.entry("VISA",          new String[]{"Visa",          "📄"}),
            Map.entry("MISCELLANEOUS", new String[]{"Misc",          "📦"})
    );

    private static final Map<String, String[]> ACTIVITY_CAT_META = Map.ofEntries(
            Map.entry("SIGHTSEEING",  new String[]{"Sightseeing",  "📸"}),
            Map.entry("ADVENTURE",    new String[]{"Adventure",    "🧗"}),
            Map.entry("CULTURAL",     new String[]{"Cultural",     "🎭"}),
            Map.entry("FOOD",         new String[]{"Food & Dining","🍜"}),
            Map.entry("SHOPPING",     new String[]{"Shopping",     "🛍️"}),
            Map.entry("NATURE",       new String[]{"Nature",       "🌿"}),
            Map.entry("NIGHTLIFE",    new String[]{"Nightlife",    "🌙"}),
            Map.entry("LEISURE",      new String[]{"Leisure",      "☀️"}),
            Map.entry("TRANSPORT",    new String[]{"Transport",    "🚌"}),
            Map.entry("ATTRACTION",   new String[]{"Attraction",   "🎡"}),
            Map.entry("FAMILY",       new String[]{"Family",       "👨‍👩‍👧"}),
            Map.entry("WORKSHOP",     new String[]{"Workshop",     "🎓"})
    );

    @Transactional(readOnly = true)
    public TripAnalyticsResponse getTripAnalytics(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));

        // Access check
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isCollaborator = collaboratorRepository.findByTripIdAndUserId(trip.getId(), user.getId()).isPresent();
        if (!isOwner && !isCollaborator) {
            throw new UnauthorizedException("Access denied to this trip");
        }

        String currency = trip.getCurrency() != null ? trip.getCurrency() : "USD";
        int totalDays = (int) ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        if (totalDays < 1) totalDays = 1;

        // ── Fetch data ──
        List<Expense> expenses = expenseRepository.findByTripIdOrderByExpenseDateDesc(tripId);
        List<CategoryBudget> categoryBudgets = categoryBudgetRepository.findByTripId(tripId);
        List<Activity> activities = activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(tripId);
        List<TransportDetail> transports = transportDetailRepository.findByTripIdOrderByDepartureTimeAsc(tripId);

        BigDecimal totalBudget = trip.getBudgetTotal() != null ? trip.getBudgetTotal() : BigDecimal.ZERO;
        BigDecimal totalSpent = expenseRepository.sumBaseCurrencyByTripId(tripId);
        BigDecimal totalRemaining = totalBudget.subtract(totalSpent);
        double budgetUsedPercent = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(totalBudget, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;

        // ── Budget vs Actual ──
        List<CategoryBudgetComparison> budgetVsActual = buildBudgetVsActual(tripId, categoryBudgets, currency);

        // ── Time Allocation ──
        int totalActivityMinutes = calculateActivityMinutes(activities);
        int totalTransitMinutes = calculateTransitMinutes(transports);
        int totalAvailableMinutes = totalDays * 14 * 60; // assume 14 usable hours/day
        int freeMinutes = Math.max(0, totalAvailableMinutes - totalActivityMinutes - totalTransitMinutes);
        double activityPercent = totalAvailableMinutes > 0 ? (totalActivityMinutes * 100.0 / totalAvailableMinutes) : 0;
        double transitPercent = totalAvailableMinutes > 0 ? (totalTransitMinutes * 100.0 / totalAvailableMinutes) : 0;
        double freeTimePercent = totalAvailableMinutes > 0 ? (freeMinutes * 100.0 / totalAvailableMinutes) : 100;

        List<CategoryTimeAllocation> timeByCategory = buildTimeByCategory(activities);

        // ── Transit vs Activity Ratio ──
        String ratio = totalTransitMinutes > 0
                ? String.format("1:%.1f", (double) totalActivityMinutes / totalTransitMinutes)
                : "0:" + totalActivityMinutes;

        // ── Spending Insights ──
        BigDecimal avgDailySpend = totalDays > 0 ? totalSpent.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal highestSingle = BigDecimal.ZERO;
        String highestTitle = "N/A";
        String topCategory = "N/A";

        if (!expenses.isEmpty()) {
            Expense highest = expenses.stream()
                    .max(Comparator.comparing(e -> e.getAmountInBaseCurrency() != null ? e.getAmountInBaseCurrency() : BigDecimal.ZERO))
                    .orElse(expenses.get(0));
            highestSingle = highest.getAmountInBaseCurrency() != null ? highest.getAmountInBaseCurrency() : highest.getAmount();
            highestTitle = highest.getTitle();

            // Top spending category
            Map<ExpenseCategory, BigDecimal> catTotals = expenses.stream()
                    .collect(Collectors.groupingBy(Expense::getCategory,
                            Collectors.reducing(BigDecimal.ZERO,
                                    e -> e.getAmountInBaseCurrency() != null ? e.getAmountInBaseCurrency() : e.getAmount(),
                                    BigDecimal::add)));
            topCategory = catTotals.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().name())
                    .orElse("N/A");
        }

        List<DailySpendTrend> dailyTrend = buildDailySpendTrend(expenses);

        return TripAnalyticsResponse.builder()
                .tripId(tripId)
                .tripTitle(trip.getTitle())
                .currency(currency)
                .totalDays(totalDays)
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .totalRemaining(totalRemaining)
                .budgetUsedPercent(Math.round(budgetUsedPercent * 10.0) / 10.0)
                .budgetVsActual(budgetVsActual)
                .totalActivityMinutes(totalActivityMinutes)
                .totalTransitMinutes(totalTransitMinutes)
                .transitPercent(Math.round(transitPercent * 10.0) / 10.0)
                .activityPercent(Math.round(activityPercent * 10.0) / 10.0)
                .freeTimePercent(Math.round(freeTimePercent * 10.0) / 10.0)
                .timeByCategory(timeByCategory)
                .transitActivityRatio(ratio)
                .totalTransportLegs(transports.size())
                .totalActivities(activities.size())
                .avgTransitMinutesPerDay(Math.round(totalTransitMinutes * 10.0 / totalDays) / 10.0)
                .avgActivityMinutesPerDay(Math.round(totalActivityMinutes * 10.0 / totalDays) / 10.0)
                .avgDailySpend(avgDailySpend)
                .highestSingleExpense(highestSingle)
                .highestExpenseTitle(highestTitle)
                .topSpendingCategory(topCategory)
                .dailySpendTrend(dailyTrend)
                .build();
    }

    // ── Helpers ──

    private List<CategoryBudgetComparison> buildBudgetVsActual(Long tripId, List<CategoryBudget> budgets, String currency) {
        Set<String> seen = new HashSet<>();
        List<CategoryBudgetComparison> result = new ArrayList<>();

        for (CategoryBudget cb : budgets) {
            String cat = cb.getCategory().name();
            seen.add(cat);
            BigDecimal budgeted = cb.getAllocatedAmount();
            BigDecimal actual = expenseRepository.sumBaseCurrencyByTripIdAndCategory(tripId, cb.getCategory());
            BigDecimal variance = budgeted.subtract(actual);
            double pct = budgeted.compareTo(BigDecimal.ZERO) > 0
                    ? actual.divide(budgeted, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;
            String status = pct > 100 ? "OVER" : pct > 80 ? "ON_TRACK" : "UNDER";
            String[] meta = CAT_META.getOrDefault(cat, new String[]{cat, "📦"});

            result.add(CategoryBudgetComparison.builder()
                    .category(cat).categoryLabel(meta[0]).emoji(meta[1])
                    .budgeted(budgeted).actual(actual).variance(variance)
                    .percentUsed(Math.round(pct * 10.0) / 10.0).status(status)
                    .build());
        }

        // Add categories with spending but no budget
        for (ExpenseCategory ec : ExpenseCategory.values()) {
            if (!seen.contains(ec.name())) {
                BigDecimal actual = expenseRepository.sumBaseCurrencyByTripIdAndCategory(tripId, ec);
                if (actual.compareTo(BigDecimal.ZERO) > 0) {
                    String[] meta = CAT_META.getOrDefault(ec.name(), new String[]{ec.name(), "📦"});
                    result.add(CategoryBudgetComparison.builder()
                            .category(ec.name()).categoryLabel(meta[0]).emoji(meta[1])
                            .budgeted(BigDecimal.ZERO).actual(actual).variance(actual.negate())
                            .percentUsed(0).status("NO_BUDGET")
                            .build());
                }
            }
        }

        return result;
    }

    private int calculateActivityMinutes(List<Activity> activities) {
        int total = 0;
        for (Activity a : activities) {
            if (a.getStartTime() != null && a.getEndTime() != null) {
                long mins = Duration.between(a.getStartTime(), a.getEndTime()).toMinutes();
                if (mins > 0) total += (int) mins;
            } else {
                total += 120; // default 2 hours per activity
            }
        }
        return total;
    }

    private int calculateTransitMinutes(List<TransportDetail> transports) {
        int total = 0;
        for (TransportDetail t : transports) {
            if (t.getDepartureTime() != null && t.getArrivalTime() != null) {
                long mins = Duration.between(t.getDepartureTime(), t.getArrivalTime()).toMinutes();
                if (mins > 0) total += (int) mins;
            } else {
                total += 60; // default 1 hour per transport
            }
        }
        return total;
    }

    private List<CategoryTimeAllocation> buildTimeByCategory(List<Activity> activities) {
        Map<String, int[]> catMap = new LinkedHashMap<>(); // [totalMinutes, count]

        for (Activity a : activities) {
            String cat = a.getCategory().name();
            int mins = 120; // default
            if (a.getStartTime() != null && a.getEndTime() != null) {
                long m = Duration.between(a.getStartTime(), a.getEndTime()).toMinutes();
                if (m > 0) mins = (int) m;
            }
            catMap.computeIfAbsent(cat, k -> new int[]{0, 0});
            catMap.get(cat)[0] += mins;
            catMap.get(cat)[1] += 1;
        }

        int grandTotal = catMap.values().stream().mapToInt(v -> v[0]).sum();

        return catMap.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
                .map(entry -> {
                    String cat = entry.getKey();
                    int mins = entry.getValue()[0];
                    int count = entry.getValue()[1];
                    double pct = grandTotal > 0 ? (mins * 100.0 / grandTotal) : 0;
                    String[] meta = ACTIVITY_CAT_META.getOrDefault(cat, new String[]{cat, "📌"});

                    return CategoryTimeAllocation.builder()
                            .category(cat).categoryLabel(meta[0]).emoji(meta[1])
                            .totalMinutes(mins).percent(Math.round(pct * 10.0) / 10.0)
                            .activityCount(count)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<DailySpendTrend> buildDailySpendTrend(List<Expense> expenses) {
        Map<LocalDate, BigDecimal[]> dateMap = new TreeMap<>(); // [amount, count]

        for (Expense e : expenses) {
            if (e.getExpenseDate() != null) {
                dateMap.computeIfAbsent(e.getExpenseDate(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                BigDecimal[] vals = dateMap.get(e.getExpenseDate());
                vals[0] = vals[0].add(e.getAmountInBaseCurrency() != null ? e.getAmountInBaseCurrency() : e.getAmount());
                vals[1] = vals[1].add(BigDecimal.ONE);
            }
        }

        return dateMap.entrySet().stream()
                .map(entry -> DailySpendTrend.builder()
                        .date(entry.getKey().toString())
                        .amount(entry.getValue()[0])
                        .expenseCount(entry.getValue()[1].intValue())
                        .build())
                .collect(Collectors.toList());
    }
}
