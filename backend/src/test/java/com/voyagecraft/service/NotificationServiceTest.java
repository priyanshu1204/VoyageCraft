package com.voyagecraft.service;

import com.voyagecraft.dto.notification.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
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
class NotificationServiceTest {

    @InjectMocks private NotificationService service;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationPreferenceRepository preferenceRepository;
    @Mock private TripRepository tripRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;

    private User testUser;
    private Trip testTrip;
    private Notification testNotif;
    private NotificationPreference testPref;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser)
                .startDate(LocalDate.now().plusDays(2)).endDate(LocalDate.now().plusDays(10))
                .budgetTotal(BigDecimal.valueOf(5000)).currency("USD")
                .createdAt(LocalDateTime.now()).build();
        testTrip.setDestinations(new ArrayList<>());
        testTrip.setCollaborators(new ArrayList<>());
        testNotif = Notification.builder().id(1L).user(testUser).trip(testTrip)
                .notificationType(NotificationType.DEPARTURE_REMINDER).severity(AlertSeverity.HIGH)
                .title("Departure soon").message("Pack bags").readStatus(false).dismissed(false)
                .createdAt(LocalDateTime.now()).build();
        testPref = NotificationPreference.builder().id(1L).user(testUser)
                .departureReminders(true).checkinReminders(true).collaboratorAlerts(true)
                .budgetAlerts(true).weatherAlerts(true).quietHoursEnabled(false)
                .budgetThresholdPercent(80).departureReminderHours(24)
                .quietHoursStart(LocalTime.of(22, 0)).quietHoursEnd(LocalTime.of(7, 0)).build();
    }

    // ── getUserNotifications ──
    @Test void getUserNotifications_success() {
        when(notificationRepository.findByUserIdAndDismissedFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testNotif));
        List<NotificationResponse> result = service.getUserNotifications(testUser);
        assertEquals(1, result.size());
        assertEquals("Departure soon", result.get(0).getTitle());
    }
    @Test void getUserNotifications_empty() {
        when(notificationRepository.findByUserIdAndDismissedFalseOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.emptyList());
        assertTrue(service.getUserNotifications(testUser).isEmpty());
    }

    // ── getUnreadNotifications ──
    @Test void getUnreadNotifications_success() {
        when(notificationRepository.findByUserIdAndReadStatusFalseAndDismissedFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testNotif));
        List<NotificationResponse> result = service.getUnreadNotifications(testUser);
        assertEquals(1, result.size());
    }

    // ── getUnreadCount ──
    @Test void getUnreadCount_success() {
        when(notificationRepository.countByUserIdAndReadStatusFalseAndDismissedFalse(1L)).thenReturn(5L);
        assertEquals(5, service.getUnreadCount(testUser));
    }
    @Test void getUnreadCount_zero() {
        when(notificationRepository.countByUserIdAndReadStatusFalseAndDismissedFalse(1L)).thenReturn(0L);
        assertEquals(0, service.getUnreadCount(testUser));
    }

    // ── getByType ──
    @Test void getByType_success() {
        when(notificationRepository.findByUserIdAndNotificationTypeAndDismissedFalseOrderByCreatedAtDesc(1L, NotificationType.DEPARTURE_REMINDER))
                .thenReturn(List.of(testNotif));
        List<NotificationResponse> result = service.getByType(testUser, "DEPARTURE_REMINDER");
        assertEquals(1, result.size());
    }

    // ── markAsRead ──
    @Test void markAsRead_success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotif));
        when(notificationRepository.save(any())).thenReturn(testNotif);
        service.markAsRead(1L, testUser);
        verify(notificationRepository).save(any());
    }
    @Test void markAsRead_notFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.markAsRead(99L, testUser));
    }
    @Test void markAsRead_wrongUser() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotif));
        assertThrows(RuntimeException.class, () -> service.markAsRead(1L, other));
    }

    // ── markAllAsRead ──
    @Test void markAllAsRead_success() {
        service.markAllAsRead(testUser);
        verify(notificationRepository).markAllAsRead(1L);
    }

    // ── dismiss ──
    @Test void dismiss_success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotif));
        when(notificationRepository.save(any())).thenReturn(testNotif);
        service.dismiss(1L, testUser);
        verify(notificationRepository).save(any());
    }
    @Test void dismiss_notFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.dismiss(99L, testUser));
    }
    @Test void dismiss_wrongUser() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotif));
        assertThrows(RuntimeException.class, () -> service.dismiss(1L, other));
    }

    // ── generateTripAlerts ──
    @Test void generateTripAlerts_departureReminder() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPref));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(expenseRepository.sumBaseCurrencyByTripId(1L)).thenReturn(BigDecimal.ZERO);
        when(collaboratorRepository.findByTripId(1L)).thenReturn(Collections.emptyList());

        List<NotificationResponse> result = service.generateTripAlerts(1L, testUser);
        assertNotNull(result);
    }

    @Test void generateTripAlerts_budgetAlert() {
        testTrip.setStartDate(LocalDate.now().plusDays(30)); // far away, no departure reminder
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPref));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(expenseRepository.sumBaseCurrencyByTripId(1L)).thenReturn(BigDecimal.valueOf(4500)); // 90%
        when(collaboratorRepository.findByTripId(1L)).thenReturn(Collections.emptyList());

        List<NotificationResponse> result = service.generateTripAlerts(1L, testUser);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test void generateTripAlerts_collaboratorPending() {
        testTrip.setStartDate(LocalDate.now().plusDays(30));
        TripCollaborator pending = TripCollaborator.builder()
                .invitationStatus(InvitationStatus.PENDING).user(testUser).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPref));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(expenseRepository.sumBaseCurrencyByTripId(1L)).thenReturn(BigDecimal.ZERO);
        when(collaboratorRepository.findByTripId(1L)).thenReturn(List.of(pending));

        List<NotificationResponse> result = service.generateTripAlerts(1L, testUser);
        assertNotNull(result);
    }

    @Test void generateTripAlerts_tripNotFound() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.generateTripAlerts(99L, testUser));
    }

    // ── getPreferences ──
    @Test void getPreferences_existing() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPref));
        NotificationPreferenceResponse resp = service.getPreferences(testUser);
        assertNotNull(resp);
        assertTrue(resp.getDepartureReminders());
    }
    @Test void getPreferences_createsDefault() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any())).thenReturn(testPref);
        NotificationPreferenceResponse resp = service.getPreferences(testUser);
        assertNotNull(resp);
    }

    // ── updatePreferences ──
    @Test void updatePreferences_allFields() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPref));
        when(preferenceRepository.save(any())).thenReturn(testPref);
        NotificationPreferenceRequest req = new NotificationPreferenceRequest();
        req.setDepartureReminders(false); req.setCheckinReminders(false);
        req.setCollaboratorAlerts(false); req.setBudgetAlerts(false);
        req.setWeatherAlerts(false); req.setQuietHoursEnabled(true);
        req.setQuietHoursStart("23:00"); req.setQuietHoursEnd("08:00");
        req.setBudgetThresholdPercent(90); req.setDepartureReminderHours(48);
        NotificationPreferenceResponse resp = service.updatePreferences(req, testUser);
        assertNotNull(resp);
    }
    @Test void updatePreferences_partialFields() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPref));
        when(preferenceRepository.save(any())).thenReturn(testPref);
        NotificationPreferenceRequest req = new NotificationPreferenceRequest();
        req.setDepartureReminders(false);
        NotificationPreferenceResponse resp = service.updatePreferences(req, testUser);
        assertNotNull(resp);
    }
}
