package com.voyagecraft.entity;

import com.voyagecraft.enums.ExpenseCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency; // original currency of expense

    @Column(precision = 12, scale = 2)
    private BigDecimal amountInBaseCurrency; // converted to trip's base currency

    @Column(precision = 12, scale = 6)
    private BigDecimal exchangeRate; // rate used for conversion

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(length = 200)
    private String paidBy; // who paid

    @Column(length = 100)
    private String receiptUrl;

    @Column(length = 500)
    private String notes;

    @Builder.Default
    private Boolean isReimbursable = false;

    @Builder.Default
    private Boolean isReimbursed = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
