package com.voyagecraft.service;

import com.voyagecraft.dto.analytics.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalyticsServiceTest {

    @InjectMocks private AnalyticsService service;
    @Mock private TripRepository tripRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private CategoryBudgetRepository categoryBudgetRepository;
    @Mock private ActivityRepository activityRepository;
    @Mock private TransportDetailRepository transportRepository;
    @Mock private StayRepository stayRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;
    @Mock private ItineraryRepository itineraryRepository;

    private User testUser;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser)
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .budgetTotal(BigDecimal.valueOf(5000)).currency("USD")
                .createdAt(LocalDateTime.now()).build();
        testTrip.setDestinations(new ArrayList<>());
        testTrip.setCollaborators(new ArrayList<>());
    }

    @Test
    void getTripAnalytics_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.findByTripIdOrderByExpenseDateDesc(1L)).thenReturn(Collections.emptyList());
        when(expenseRepository.sumBaseCurrencyByTripId(1L)).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.sumBaseCurrencyByTripIdAndCategory(eq(1L), any())).thenReturn(BigDecimal.ZERO);
        when(categoryBudgetRepository.findByTripIdAndCategory(eq(1L), any())).thenReturn(Optional.empty());
        when(activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(1L)).thenReturn(Collections.emptyList());
        when(transportRepository.findByTripIdOrderByDepartureTimeAsc(1L)).thenReturn(Collections.emptyList());
        when(stayRepository.findByTripIdOrderByCheckInDateAsc(1L)).thenReturn(Collections.emptyList());

        TripAnalyticsResponse resp = service.getTripAnalytics(1L, testUser);
        assertNotNull(resp);
        assertEquals(1L, resp.getTripId());
    }

    @Test
    void getTripAnalytics_tripNotFound_throwsException() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getTripAnalytics(99L, testUser));
    }

    @Test
    void getTripAnalytics_noAccess_throwsException() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> service.getTripAnalytics(1L, other));
    }

    @Test
    void getTripAnalytics_withExpenses() {
        Expense e = Expense.builder().id(1L).trip(testTrip).title("Hotel")
                .category(ExpenseCategory.STAY).amount(BigDecimal.valueOf(500))
                .currency("USD").amountInBaseCurrency(BigDecimal.valueOf(500))
                .expenseDate(LocalDate.now()).createdAt(LocalDateTime.now()).build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.findByTripIdOrderByExpenseDateDesc(1L)).thenReturn(List.of(e));
        when(expenseRepository.sumBaseCurrencyByTripId(1L)).thenReturn(BigDecimal.valueOf(500));
        when(expenseRepository.sumBaseCurrencyByTripIdAndCategory(eq(1L), any())).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.sumBaseCurrencyByTripIdAndCategory(1L, ExpenseCategory.STAY)).thenReturn(BigDecimal.valueOf(500));
        when(categoryBudgetRepository.findByTripIdAndCategory(eq(1L), any())).thenReturn(Optional.empty());
        when(activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(1L)).thenReturn(Collections.emptyList());
        when(transportRepository.findByTripIdOrderByDepartureTimeAsc(1L)).thenReturn(Collections.emptyList());
        when(stayRepository.findByTripIdOrderByCheckInDateAsc(1L)).thenReturn(Collections.emptyList());

        TripAnalyticsResponse resp = service.getTripAnalytics(1L, testUser);
        assertNotNull(resp);
    }
}
