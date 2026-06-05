package com.voyagecraft.dto.budget;

import com.voyagecraft.enums.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseRequest {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Expense date is required")
    private String expenseDate;

    private String paidBy;
    private String receiptUrl;
    private String notes;
    private Boolean isReimbursable;
}
