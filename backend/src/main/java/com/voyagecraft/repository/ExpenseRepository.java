package com.voyagecraft.repository;

import com.voyagecraft.entity.Expense;
import com.voyagecraft.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByTripIdOrderByExpenseDateDesc(Long tripId);

    List<Expense> findByTripIdAndCategory(Long tripId, ExpenseCategory category);

    @Query("SELECT COALESCE(SUM(e.amountInBaseCurrency), 0) FROM Expense e WHERE e.trip.id = :tripId")
    BigDecimal sumBaseCurrencyByTripId(@Param("tripId") Long tripId);

    @Query("SELECT COALESCE(SUM(e.amountInBaseCurrency), 0) FROM Expense e WHERE e.trip.id = :tripId AND e.category = :category")
    BigDecimal sumBaseCurrencyByTripIdAndCategory(@Param("tripId") Long tripId, @Param("category") ExpenseCategory category);

    long countByTripId(Long tripId);
}
