package com.voyagecraft.service;

import com.voyagecraft.dto.budget.*;
import com.voyagecraft.entity.CategoryBudget;
import com.voyagecraft.entity.Expense;
import com.voyagecraft.entity.Trip;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.ExpenseCategory;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.CategoryBudgetRepository;
import com.voyagecraft.repository.ExpenseRepository;
import com.voyagecraft.repository.TripCollaboratorRepository;
import com.voyagecraft.repository.TripRepository;
import com.voyagecraft.enums.CollaboratorRole;
import com.voyagecraft.enums.InvitationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final ExpenseRepository expenseRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;

    // ── Mock exchange rates (base = USD) ─────────────────────────────────
    private static final Map<String, BigDecimal> EXCHANGE_RATES = Map.ofEntries(
        Map.entry("USD", BigDecimal.ONE),
        Map.entry("EUR", new BigDecimal("0.92")),
        Map.entry("GBP", new BigDecimal("0.79")),
        Map.entry("INR", new BigDecimal("83.50")),
        Map.entry("JPY", new BigDecimal("154.20")),
        Map.entry("AUD", new BigDecimal("1.53")),
        Map.entry("CAD", new BigDecimal("1.37")),
        Map.entry("SGD", new BigDecimal("1.34")),
        Map.entry("THB", new BigDecimal("35.80")),
        Map.entry("MYR", new BigDecimal("4.72")),
        Map.entry("AED", new BigDecimal("3.67")),
        Map.entry("CHF", new BigDecimal("0.88")),
        Map.entry("CNY", new BigDecimal("7.24")),
        Map.entry("KRW", new BigDecimal("1350.00")),
        Map.entry("NZD", new BigDecimal("1.68"))
    );

    // ── Expense CRUD ─────────────────────────────────────────────────────

    @Transactional
    public ExpenseResponse addExpense(ExpenseRequest request, User user) {
        Trip trip = getTripWithOwnerCheck(request.getTripId(), user);
        String baseCurrency = trip.getCurrency() != null ? trip.getCurrency() : "USD";

        BigDecimal exchangeRate = calculateExchangeRate(request.getCurrency(), baseCurrency);
        BigDecimal amountInBase = request.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

        Expense expense = Expense.builder()
                .trip(trip)
                .title(request.getTitle().trim())
                .description(blankToNull(request.getDescription()))
                .category(request.getCategory())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .amountInBaseCurrency(amountInBase)
                .exchangeRate(exchangeRate)
                .expenseDate(LocalDate.parse(request.getExpenseDate()))
                .paidBy(blankToNull(request.getPaidBy()))
                .receiptUrl(blankToNull(request.getReceiptUrl()))
                .notes(blankToNull(request.getNotes()))
                .isReimbursable(request.getIsReimbursable() != null ? request.getIsReimbursable() : false)
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Added expense '{}' ({} {}) for trip ID={}", request.getTitle(), request.getAmount(), request.getCurrency(), trip.getId());
        return mapExpense(saved);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long expenseId, ExpenseRequest request, User user) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + expenseId));
        Trip trip = getTripWithOwnerCheck(expense.getTrip().getId(), user);
        String baseCurrency = trip.getCurrency() != null ? trip.getCurrency() : "USD";

        BigDecimal exchangeRate = calculateExchangeRate(request.getCurrency(), baseCurrency);
        BigDecimal amountInBase = request.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

        expense.setTitle(request.getTitle().trim());
        expense.setDescription(blankToNull(request.getDescription()));
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setCurrency(request.getCurrency().toUpperCase());
        expense.setAmountInBaseCurrency(amountInBase);
        expense.setExchangeRate(exchangeRate);
        expense.setExpenseDate(LocalDate.parse(request.getExpenseDate()));
        expense.setPaidBy(blankToNull(request.getPaidBy()));
        expense.setReceiptUrl(blankToNull(request.getReceiptUrl()));
        expense.setNotes(blankToNull(request.getNotes()));
        expense.setIsReimbursable(request.getIsReimbursable() != null ? request.getIsReimbursable() : false);

        return mapExpense(expenseRepository.save(expense));
    }

    @Transactional
    public void deleteExpense(Long expenseId, User user) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + expenseId));
        getTripWithOwnerCheck(expense.getTrip().getId(), user);
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getTripExpenses(Long tripId, User user) {
        getTripWithOwnerCheck(tripId, user);
        return expenseRepository.findByTripIdOrderByExpenseDateDesc(tripId).stream()
                .map(this::mapExpense)
                .collect(Collectors.toList());
    }

    // ── Category Budgets ─────────────────────────────────────────────────

    @Transactional
    public CategoryBudgetResponse setCategoryBudget(CategoryBudgetRequest request, User user) {
        getTripWithOwnerCheck(request.getTripId(), user);

        CategoryBudget budget = categoryBudgetRepository
                .findByTripIdAndCategory(request.getTripId(), request.getCategory())
                .orElse(null);

        if (budget == null) {
            budget = CategoryBudget.builder()
                    .trip(tripRepository.getReferenceById(request.getTripId()))
                    .category(request.getCategory())
                    .allocatedAmount(request.getAllocatedAmount())
                    .build();
        } else {
            budget.setAllocatedAmount(request.getAllocatedAmount());
        }

        categoryBudgetRepository.save(budget);
        BigDecimal spent = expenseRepository.sumBaseCurrencyByTripIdAndCategory(request.getTripId(), request.getCategory());
        return mapCategoryBudget(budget, spent);
    }

    @Transactional(readOnly = true)
    public List<CategoryBudgetResponse> getCategoryBudgets(Long tripId, User user) {
        getTripWithOwnerCheck(tripId, user);
        List<CategoryBudget> budgets = categoryBudgetRepository.findByTripId(tripId);

        return budgets.stream().map(b -> {
            BigDecimal spent = expenseRepository.sumBaseCurrencyByTripIdAndCategory(tripId, b.getCategory());
            return mapCategoryBudget(b, spent);
        }).collect(Collectors.toList());
    }

    // ── Budget Summary ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public BudgetSummaryResponse getBudgetSummary(Long tripId, User user) {
        Trip trip = getTripWithOwnerCheck(tripId, user);
        String baseCurrency = trip.getCurrency() != null ? trip.getCurrency() : "USD";

        BigDecimal totalBudget = trip.getBudgetTotal() != null ? trip.getBudgetTotal() : BigDecimal.ZERO;
        BigDecimal totalSpent = expenseRepository.sumBaseCurrencyByTripId(tripId);
        BigDecimal totalRemaining = totalBudget.subtract(totalSpent);
        double overallPercent = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(totalBudget, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;

        int totalExpenses = (int) expenseRepository.countByTripId(tripId);

        // Category breakdown
        List<CategoryBudgetResponse> categoryBreakdown = new ArrayList<>();
        for (ExpenseCategory cat : ExpenseCategory.values()) {
            BigDecimal spent = expenseRepository.sumBaseCurrencyByTripIdAndCategory(tripId, cat);
            CategoryBudget catBudget = categoryBudgetRepository.findByTripIdAndCategory(tripId, cat).orElse(null);
            BigDecimal allocated = catBudget != null ? catBudget.getAllocatedAmount() : BigDecimal.ZERO;

            if (spent.compareTo(BigDecimal.ZERO) > 0 || allocated.compareTo(BigDecimal.ZERO) > 0) {
                categoryBreakdown.add(CategoryBudgetResponse.builder()
                        .id(catBudget != null ? catBudget.getId() : null)
                        .category(cat)
                        .allocatedAmount(allocated)
                        .spentAmount(spent)
                        .remainingAmount(allocated.subtract(spent))
                        .percentUsed(allocated.compareTo(BigDecimal.ZERO) > 0
                                ? spent.divide(allocated, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0)
                        .build());
            }
        }

        // Daily spending
        List<Expense> allExpenses = expenseRepository.findByTripIdOrderByExpenseDateDesc(tripId);
        Map<LocalDate, BigDecimal> dailyMap = new TreeMap<>();
        for (Expense e : allExpenses) {
            dailyMap.merge(e.getExpenseDate(), e.getAmountInBaseCurrency(), BigDecimal::add);
        }
        List<DailySpendResponse> dailySpending = dailyMap.entrySet().stream()
                .map(entry -> DailySpendResponse.builder().date(entry.getKey()).amount(entry.getValue()).build())
                .collect(Collectors.toList());

        return BudgetSummaryResponse.builder()
                .tripId(tripId)
                .baseCurrency(baseCurrency)
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .totalRemaining(totalRemaining)
                .overallPercentUsed(overallPercent)
                .totalExpenses(totalExpenses)
                .categoryBreakdown(categoryBreakdown)
                .dailySpending(dailySpending)
                .build();
    }

    // ── Export CSV ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public String exportExpensesCsv(Long tripId, User user) {
        Trip trip = getTripWithOwnerCheck(tripId, user);
        String baseCurrency = trip.getCurrency() != null ? trip.getCurrency() : "USD";
        List<Expense> expenses = expenseRepository.findByTripIdOrderByExpenseDateDesc(tripId);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Title,Category,Amount,Currency,Amount (").append(baseCurrency).append("),Exchange Rate,Paid By,Notes\n");

        for (Expense e : expenses) {
            csv.append(e.getExpenseDate()).append(",");
            csv.append(escapeCsv(e.getTitle())).append(",");
            csv.append(e.getCategory()).append(",");
            csv.append(e.getAmount()).append(",");
            csv.append(e.getCurrency()).append(",");
            csv.append(e.getAmountInBaseCurrency()).append(",");
            csv.append(e.getExchangeRate()).append(",");
            csv.append(escapeCsv(e.getPaidBy())).append(",");
            csv.append(escapeCsv(e.getNotes())).append("\n");
        }

        // Summary section
        BigDecimal totalSpent = expenseRepository.sumBaseCurrencyByTripId(tripId);
        BigDecimal totalBudget = trip.getBudgetTotal() != null ? trip.getBudgetTotal() : BigDecimal.ZERO;

        csv.append("\n--- SUMMARY ---\n");
        csv.append("Total Budget,").append(totalBudget).append(",").append(baseCurrency).append("\n");
        csv.append("Total Spent,").append(totalSpent).append(",").append(baseCurrency).append("\n");
        csv.append("Remaining,").append(totalBudget.subtract(totalSpent)).append(",").append(baseCurrency).append("\n");

        // Category subtotals
        csv.append("\n--- CATEGORY BREAKDOWN ---\n");
        csv.append("Category,Budget,Spent,Remaining\n");
        for (ExpenseCategory cat : ExpenseCategory.values()) {
            BigDecimal spent = expenseRepository.sumBaseCurrencyByTripIdAndCategory(tripId, cat);
            CategoryBudget catBudget = categoryBudgetRepository.findByTripIdAndCategory(tripId, cat).orElse(null);
            BigDecimal allocated = catBudget != null ? catBudget.getAllocatedAmount() : BigDecimal.ZERO;
            if (spent.compareTo(BigDecimal.ZERO) > 0 || allocated.compareTo(BigDecimal.ZERO) > 0) {
                csv.append(cat).append(",").append(allocated).append(",").append(spent).append(",")
                   .append(allocated.subtract(spent)).append("\n");
            }
        }

        return csv.toString();
    }

    // ── Currency Helpers ─────────────────────────────────────────────────

    public Map<String, BigDecimal> getSupportedCurrencies() {
        return Collections.unmodifiableMap(EXCHANGE_RATES);
    }

    private BigDecimal calculateExchangeRate(String fromCurrency, String toCurrency) {
        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();

        if (from.equals(to)) return BigDecimal.ONE;

        BigDecimal fromRate = EXCHANGE_RATES.getOrDefault(from, BigDecimal.ONE);
        BigDecimal toRate = EXCHANGE_RATES.getOrDefault(to, BigDecimal.ONE);

        // Convert: amount_in_from / fromRate * toRate = amount_in_to
        return toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Trip getTripWithOwnerCheck(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isEditor = collaboratorRepository.findByTripIdAndUserId(tripId, user.getId())
                .map(c -> c.getInvitationStatus() == InvitationStatus.ACCEPTED
                        && (c.getRole() == CollaboratorRole.OWNER || c.getRole() == CollaboratorRole.EDITOR))
                .orElse(false);
        if (!isOwner && !isEditor) {
            throw new UnauthorizedException("Access denied to this trip");
        }
        return trip;
    }

    private ExpenseResponse mapExpense(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId())
                .tripId(e.getTrip().getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .category(e.getCategory())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .amountInBaseCurrency(e.getAmountInBaseCurrency())
                .exchangeRate(e.getExchangeRate())
                .expenseDate(e.getExpenseDate())
                .paidBy(e.getPaidBy())
                .receiptUrl(e.getReceiptUrl())
                .notes(e.getNotes())
                .isReimbursable(e.getIsReimbursable())
                .isReimbursed(e.getIsReimbursed())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private CategoryBudgetResponse mapCategoryBudget(CategoryBudget b, BigDecimal spent) {
        BigDecimal allocated = b.getAllocatedAmount();
        BigDecimal remaining = allocated.subtract(spent);
        double percent = allocated.compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(allocated, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;

        return CategoryBudgetResponse.builder()
                .id(b.getId())
                .category(b.getCategory())
                .allocatedAmount(allocated)
                .spentAmount(spent)
                .remainingAmount(remaining)
                .percentUsed(percent)
                .build();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** Convert blank/empty strings to null so nullable DB columns don't get empty-string values. */
    private String blankToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}
