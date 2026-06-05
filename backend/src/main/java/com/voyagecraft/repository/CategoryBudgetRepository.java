package com.voyagecraft.repository;

import com.voyagecraft.entity.CategoryBudget;
import com.voyagecraft.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {

    List<CategoryBudget> findByTripId(Long tripId);

    Optional<CategoryBudget> findByTripIdAndCategory(Long tripId, ExpenseCategory category);
}
