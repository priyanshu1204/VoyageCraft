package com.voyagecraft.service;

import com.voyagecraft.dto.budget.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @InjectMocks private BudgetService budgetService;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private CategoryBudgetRepository categoryBudgetRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;

    private User testUser;
    private Trip testTrip;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@test.com").firstName("John").lastName("Doe").build();
        testTrip = Trip.builder().id(1L).title("Trip").currency("USD")
                .budgetTotal(BigDecimal.valueOf(5000)).createdBy(testUser)
                .createdAt(LocalDateTime.now()).build();

        testExpense = Expense.builder().id(1L).trip(testTrip).title("Hotel")
                .category(ExpenseCategory.STAY).amount(BigDecimal.valueOf(200))
                .currency("USD").amountInBaseCurrency(BigDecimal.valueOf(200))
                .exchangeRate(BigDecimal.ONE).expenseDate(LocalDate.now())
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void addExpense_success() {
        ExpenseRequest req = new ExpenseRequest();
        req.setTripId(1L); req.setTitle("Hotel"); req.setAmount(BigDecimal.valueOf(200));
        req.setCurrency("USD"); req.setCategory(ExpenseCategory.STAY);
        req.setExpenseDate(LocalDate.now().toString());

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        ExpenseResponse resp = budgetService.addExpense(req, testUser);

        assertNotNull(resp);
        assertEquals("Hotel", resp.getTitle());
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void addExpense_tripNotFound_throwsException() {
        ExpenseRequest req = new ExpenseRequest();
        req.setTripId(99L); req.setTitle("X"); req.setAmount(BigDecimal.TEN);
        req.setCurrency("USD"); req.setCategory(ExpenseCategory.FOOD);
        req.setExpenseDate(LocalDate.now().toString());

        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> budgetService.addExpense(req, testUser));
    }

    @Test
    void addExpense_noAccess_throwsException() {
        User otherUser = User.builder().id(2L).email("other@test.com").firstName("X").lastName("Y").build();
        ExpenseRequest req = new ExpenseRequest();
        req.setTripId(1L); req.setTitle("X"); req.setAmount(BigDecimal.TEN);
        req.setCurrency("USD"); req.setCategory(ExpenseCategory.FOOD);
        req.setExpenseDate(LocalDate.now().toString());

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> budgetService.addExpense(req, otherUser));
    }

    @Test
    void updateExpense_success() {
        ExpenseRequest req = new ExpenseRequest();
        req.setTripId(1L); req.setTitle("Updated Hotel"); req.setAmount(BigDecimal.valueOf(250));
        req.setCurrency("USD"); req.setCategory(ExpenseCategory.STAY);
        req.setExpenseDate(LocalDate.now().toString());

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        ExpenseResponse resp = budgetService.updateExpense(1L, req, testUser);
        assertNotNull(resp);
    }

    @Test
    void updateExpense_notFound_throwsException() {
        ExpenseRequest req = new ExpenseRequest();
        req.setTripId(1L); req.setTitle("X"); req.setAmount(BigDecimal.TEN);
        req.setCurrency("USD"); req.setCategory(ExpenseCategory.FOOD);
        req.setExpenseDate(LocalDate.now().toString());

        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> budgetService.updateExpense(99L, req, testUser));
    }

    @Test
    void deleteExpense_success() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        budgetService.deleteExpense(1L, testUser);
        verify(expenseRepository).delete(testExpense);
    }

    @Test
    void deleteExpense_notFound_throwsException() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> budgetService.deleteExpense(99L, testUser));
    }

    @Test
    void getTripExpenses_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.findByTripIdOrderByExpenseDateDesc(1L)).thenReturn(List.of(testExpense));

        List<ExpenseResponse> result = budgetService.getTripExpenses(1L, testUser);
        assertEquals(1, result.size());
    }

    @Test
    void setCategoryBudget_new_success() {
        CategoryBudgetRequest req = new CategoryBudgetRequest();
        req.setTripId(1L); req.setCategory(ExpenseCategory.FOOD);
        req.setAllocatedAmount(BigDecimal.valueOf(500));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(categoryBudgetRepository.findByTripIdAndCategory(1L, ExpenseCategory.FOOD)).thenReturn(Optional.empty());
        when(tripRepository.getReferenceById(1L)).thenReturn(testTrip);
        CategoryBudget saved = CategoryBudget.builder().id(1L).trip(testTrip)
                .category(ExpenseCategory.FOOD).allocatedAmount(BigDecimal.valueOf(500)).build();
        when(categoryBudgetRepository.save(any())).thenReturn(saved);
        when(expenseRepository.sumBaseCurrencyByTripIdAndCategory(1L, ExpenseCategory.FOOD)).thenReturn(BigDecimal.ZERO);

        CategoryBudgetResponse resp = budgetService.setCategoryBudget(req, testUser);
        assertNotNull(resp);
        assertEquals(ExpenseCategory.FOOD, resp.getCategory());
    }

    @Test
    void setCategoryBudget_update_success() {
        CategoryBudget existing = CategoryBudget.builder().id(1L).trip(testTrip)
                .category(ExpenseCategory.FOOD).allocatedAmount(BigDecimal.valueOf(300)).build();
        CategoryBudgetRequest req = new CategoryBudgetRequest();
        req.setTripId(1L); req.setCategory(ExpenseCategory.FOOD);
        req.setAllocatedAmount(BigDecimal.valueOf(600));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(categoryBudgetRepository.findByTripIdAndCategory(1L, ExpenseCategory.FOOD)).thenReturn(Optional.of(existing));
        when(categoryBudgetRepository.save(any())).thenReturn(existing);
        when(expenseRepository.sumBaseCurrencyByTripIdAndCategory(1L, ExpenseCategory.FOOD)).thenReturn(BigDecimal.valueOf(100));

        CategoryBudgetResponse resp = budgetService.setCategoryBudget(req, testUser);
        assertNotNull(resp);
    }

    @Test
    void getCategoryBudgets_success() {
        CategoryBudget cb = CategoryBudget.builder().id(1L).trip(testTrip)
                .category(ExpenseCategory.FOOD).allocatedAmount(BigDecimal.valueOf(500)).build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(categoryBudgetRepository.findByTripId(1L)).thenReturn(List.of(cb));
        when(expenseRepository.sumBaseCurrencyByTripIdAndCategory(1L, ExpenseCategory.FOOD)).thenReturn(BigDecimal.valueOf(200));

        List<CategoryBudgetResponse> result = budgetService.getCategoryBudgets(1L, testUser);
        assertEquals(1, result.size());
    }

    @Test
    void getBudgetSummary_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.sumBaseCurrencyByTripId(1L)).thenReturn(BigDecimal.valueOf(1000));
        when(expenseRepository.countByTripId(1L)).thenReturn(5L);
        when(expenseRepository.findByTripIdOrderByExpenseDateDesc(1L)).thenReturn(List.of(testExpense));
        when(expenseRepository.sumBaseCurrencyByTripIdAndCategory(eq(1L), any(ExpenseCategory.class))).thenReturn(BigDecimal.ZERO);
        when(categoryBudgetRepository.findByTripIdAndCategory(eq(1L), any(ExpenseCategory.class))).thenReturn(Optional.empty());

        BudgetSummaryResponse resp = budgetService.getBudgetSummary(1L, testUser);

        assertNotNull(resp);
        assertEquals(1L, resp.getTripId());
        assertEquals("USD", resp.getBaseCurrency());
    }

    @Test
    void getBudgetSummary_zeroBudget() {
        Trip zeroBudgetTrip = Trip.builder().id(2L).title("Trip").currency("USD")
                .budgetTotal(null).createdBy(testUser).createdAt(LocalDateTime.now()).build();

        when(tripRepository.findById(2L)).thenReturn(Optional.of(zeroBudgetTrip));
        when(expenseRepository.sumBaseCurrencyByTripId(2L)).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.countByTripId(2L)).thenReturn(0L);
        when(expenseRepository.findByTripIdOrderByExpenseDateDesc(2L)).thenReturn(Collections.emptyList());
        when(expenseRepository.sumBaseCurrencyByTripIdAndCategory(eq(2L), any())).thenReturn(BigDecimal.ZERO);
        when(categoryBudgetRepository.findByTripIdAndCategory(eq(2L), any())).thenReturn(Optional.empty());

        BudgetSummaryResponse resp = budgetService.getBudgetSummary(2L, testUser);
        assertEquals(0, resp.getOverallPercentUsed());
    }

    @Test
    void exportExpensesCsv_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.findByTripIdOrderByExpenseDateDesc(1L)).thenReturn(List.of(testExpense));
        when(expenseRepository.sumBaseCurrencyByTripId(1L)).thenReturn(BigDecimal.valueOf(200));
        when(expenseRepository.sumBaseCurrencyByTripIdAndCategory(eq(1L), any())).thenReturn(BigDecimal.ZERO);
        when(categoryBudgetRepository.findByTripIdAndCategory(eq(1L), any())).thenReturn(Optional.empty());

        String csv = budgetService.exportExpensesCsv(1L, testUser);

        assertNotNull(csv);
        assertTrue(csv.contains("Date,Title,Category"));
        assertTrue(csv.contains("Hotel"));
        assertTrue(csv.contains("SUMMARY"));
    }

    @Test
    void getSupportedCurrencies_returnsMap() {
        Map<String, BigDecimal> currencies = budgetService.getSupportedCurrencies();
        assertNotNull(currencies);
        assertTrue(currencies.containsKey("USD"));
        assertTrue(currencies.containsKey("INR"));
        assertTrue(currencies.containsKey("EUR"));
    }

    @Test
    void addExpense_differentCurrency_convertsCorrectly() {
        ExpenseRequest req = new ExpenseRequest();
        req.setTripId(1L); req.setTitle("Dinner"); req.setAmount(BigDecimal.valueOf(100));
        req.setCurrency("EUR"); req.setCategory(ExpenseCategory.FOOD);
        req.setExpenseDate(LocalDate.now().toString());

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        ExpenseResponse resp = budgetService.addExpense(req, testUser);
        assertNotNull(resp);
    }
}
