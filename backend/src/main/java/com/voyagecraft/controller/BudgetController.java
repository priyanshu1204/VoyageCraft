package com.voyagecraft.controller;

import com.voyagecraft.dto.budget.*;
import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/budget")
@RequiredArgsConstructor
@Tag(name = "Budget & Expenses", description = "Budget tracking and expense management (US-06)")
public class BudgetController {

    private final BudgetService budgetService;
    private final AuthService authService;

    // ── Expenses ─────────────────────────────────────────────────────────

    @PostMapping("/expenses")
    @Operation(summary = "Add an expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> addExpense(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense added", budgetService.addExpense(request, user)));
    }

    @PutMapping("/expenses/{id}")
    @Operation(summary = "Update an expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Expense updated", budgetService.updateExpense(id, request, user)));
    }

    @DeleteMapping("/expenses/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        budgetService.deleteExpense(id, user);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted", null));
    }

    @GetMapping("/expenses/trip/{tripId}")
    @Operation(summary = "Get all expenses for a trip")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getTripExpenses(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(budgetService.getTripExpenses(tripId, user)));
    }

    // ── Category Budgets ─────────────────────────────────────────────────

    @PostMapping("/category")
    @Operation(summary = "Set or update a category budget")
    public ResponseEntity<ApiResponse<CategoryBudgetResponse>> setCategoryBudget(
            @Valid @RequestBody CategoryBudgetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Budget set", budgetService.setCategoryBudget(request, user)));
    }

    @GetMapping("/category/trip/{tripId}")
    @Operation(summary = "Get all category budgets for a trip")
    public ResponseEntity<ApiResponse<List<CategoryBudgetResponse>>> getCategoryBudgets(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(budgetService.getCategoryBudgets(tripId, user)));
    }

    // ── Summary ──────────────────────────────────────────────────────────

    @GetMapping("/summary/trip/{tripId}")
    @Operation(summary = "Get full budget summary with category breakdown")
    public ResponseEntity<ApiResponse<BudgetSummaryResponse>> getBudgetSummary(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(budgetService.getBudgetSummary(tripId, user)));
    }

    // ── Export ────────────────────────────────────────────────────────────

    @GetMapping("/export/trip/{tripId}")
    @Operation(summary = "Export expenses as CSV")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        String csv = budgetService.exportExpensesCsv(tripId, user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trip_" + tripId + "_expenses.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    // ── Currencies ───────────────────────────────────────────────────────

    @GetMapping("/currencies")
    @Operation(summary = "Get supported currencies with exchange rates")
    public ResponseEntity<ApiResponse<Map<String, java.math.BigDecimal>>> getCurrencies() {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getSupportedCurrencies()));
    }
}
