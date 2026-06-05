package com.voyagecraft.dto.budget;

import com.voyagecraft.enums.ExpenseCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private Long tripId;
    private String title;
    private String description;
    private ExpenseCategory category;
    private BigDecimal amount;
    private String currency;
    private BigDecimal amountInBaseCurrency;
    private BigDecimal exchangeRate;
    private LocalDate expenseDate;
    private String paidBy;
    private String receiptUrl;
    private String notes;
    private Boolean isReimbursable;
    private Boolean isReimbursed;
    private LocalDateTime createdAt;
}
