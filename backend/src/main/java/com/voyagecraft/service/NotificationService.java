package com.voyagecraft.service;

import com.voyagecraft.dto.notification.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.AlertSeverity;
import com.voyagecraft.enums.NotificationType;
import com.voyagecraft.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;
    private final TripCollaboratorRepository collaboratorRepository;

    // ── Get Notifications ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(User user) {
        return notificationRepository.findByUserIdAndDismissedFalseOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(User user) {
        return notificationRepository.findByUserIdAndReadStatusFalseAndDismissedFalseOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndReadStatusFalseAndDismissedFalse(user.getId());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByType(User user, String type) {
        NotificationType nType = NotificationType.valueOf(type);
        return notificationRepository.findByUserIdAndNotificationTypeAndDismissedFalseOrderByCreatedAtDesc(user.getId(), nType)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Mark Read / Dismiss ──────────────────────────────────────────

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!n.getUser().getId().equals(user.getId())) throw new RuntimeException("Not authorized");
        n.setReadStatus(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user.getId());
    }

    @Transactional
    public void dismiss(Long notificationId, User user) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!n.getUser().getId().equals(user.getId())) throw new RuntimeException("Not authorized");
        n.setDismissed(true);
        notificationRepository.save(n);
    }

    // ── Generate Alerts ──────────────────────────────────────────────

    @Transactional
    public List<NotificationResponse> generateTripAlerts(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));
        NotificationPreference prefs = getOrCreatePreferences(user);
        List<Notification> generated = new ArrayList<>();

        // 1. Departure Reminder
        if (prefs.getDepartureReminders() && trip.getStartDate() != null) {
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), trip.getStartDate());
            if (daysUntil >= 0 && daysUntil <= 7) {
                AlertSeverity severity = daysUntil <= 1 ? AlertSeverity.CRITICAL
                        : daysUntil <= 3 ? AlertSeverity.HIGH : AlertSeverity.MEDIUM;
                generated.add(createNotification(user, trip, NotificationType.DEPARTURE_REMINDER, severity,
                        "✈️ Departure in " + daysUntil + " day(s)!",
                        "Your trip \"" + trip.getTitle() + "\" starts on " + trip.getStartDate() + ". Make sure everything is packed and ready!",
                        "/trips/" + tripId));
            }
        }

        // 2. Check-in Reminder
        if (prefs.getCheckinReminders() && trip.getStartDate() != null) {
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), trip.getStartDate());
            if (daysUntil >= 1 && daysUntil <= 2) {
                generated.add(createNotification(user, trip, NotificationType.CHECKIN_REMINDER, AlertSeverity.HIGH,
                        "🏨 Check-in reminder",
                        "Don't forget to check in for your trip \"" + trip.getTitle() + "\". Confirm your accommodation details.",
                        "/trips/" + tripId + "/stays"));
            }
        }

        // 3. Budget Threshold Alert
        if (prefs.getBudgetAlerts() && trip.getBudgetTotal() != null && trip.getBudgetTotal().compareTo(java.math.BigDecimal.ZERO) > 0) {
            try {
                java.math.BigDecimal totalSpent = expenseRepository.sumBaseCurrencyByTripId(tripId);
                double usedPercent = (totalSpent.doubleValue() / trip.getBudgetTotal().doubleValue()) * 100;

                if (usedPercent >= prefs.getBudgetThresholdPercent()) {
                    AlertSeverity severity = usedPercent >= 100 ? AlertSeverity.CRITICAL
                            : usedPercent >= 90 ? AlertSeverity.HIGH : AlertSeverity.MEDIUM;
                    generated.add(createNotification(user, trip, NotificationType.BUDGET_THRESHOLD, severity,
                            "💰 Budget alert: " + String.format("%.0f%%", usedPercent) + " used",
                            "You have spent " + String.format("%.2f", totalSpent.doubleValue()) + " of your " + trip.getBudgetTotal() + " " + trip.getCurrency() + " budget for \"" + trip.getTitle() + "\".",
                            "/trips/" + tripId + "/budget"));
                }
            } catch (Exception e) {
                log.warn("Could not calculate budget for trip {}: {}", tripId, e.getMessage());
            }
        }

        // 4. Collaborator Change alerts (informational)
        if (prefs.getCollaboratorAlerts()) {
            List<TripCollaborator> collabs = collaboratorRepository.findByTripId(tripId);
            long pendingCount = collabs.stream().filter(c -> "PENDING".equals(c.getInvitationStatus().name())).count();
            if (pendingCount > 0) {
                generated.add(createNotification(user, trip, NotificationType.COLLABORATOR_CHANGE, AlertSeverity.LOW,
                        "👥 " + pendingCount + " pending invitation(s)",
                        "You have " + pendingCount + " collaborator(s) who haven't accepted their invitation to \"" + trip.getTitle() + "\" yet.",
                        "/trips/" + tripId + "/collaborate"));
            }
        }

        return generated.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Preferences ──────────────────────────────────────────────────

    @Transactional
    public NotificationPreferenceResponse getPreferences(User user) {
        NotificationPreference prefs = getOrCreatePreferences(user);
        return mapPrefToResponse(prefs);
    }

    @Transactional
    public NotificationPreferenceResponse updatePreferences(NotificationPreferenceRequest request, User user) {
        NotificationPreference prefs = getOrCreatePreferences(user);

        if (request.getDepartureReminders() != null) prefs.setDepartureReminders(request.getDepartureReminders());
        if (request.getCheckinReminders() != null) prefs.setCheckinReminders(request.getCheckinReminders());
        if (request.getCollaboratorAlerts() != null) prefs.setCollaboratorAlerts(request.getCollaboratorAlerts());
        if (request.getBudgetAlerts() != null) prefs.setBudgetAlerts(request.getBudgetAlerts());
        if (request.getWeatherAlerts() != null) prefs.setWeatherAlerts(request.getWeatherAlerts());
        if (request.getQuietHoursEnabled() != null) prefs.setQuietHoursEnabled(request.getQuietHoursEnabled());
        if (request.getQuietHoursStart() != null) prefs.setQuietHoursStart(LocalTime.parse(request.getQuietHoursStart()));
        if (request.getQuietHoursEnd() != null) prefs.setQuietHoursEnd(LocalTime.parse(request.getQuietHoursEnd()));
        if (request.getBudgetThresholdPercent() != null) prefs.setBudgetThresholdPercent(request.getBudgetThresholdPercent());
        if (request.getDepartureReminderHours() != null) prefs.setDepartureReminderHours(request.getDepartureReminderHours());

        preferenceRepository.save(prefs);
        return mapPrefToResponse(prefs);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private NotificationPreference getOrCreatePreferences(User user) {
        return preferenceRepository.findByUserId(user.getId()).orElseGet(() -> {
            NotificationPreference newPrefs = NotificationPreference.builder()
                    .user(user)
                    .quietHoursStart(LocalTime.of(22, 0))
                    .quietHoursEnd(LocalTime.of(7, 0))
                    .build();
            return preferenceRepository.save(newPrefs);
        });
    }

    private Notification createNotification(User user, Trip trip, NotificationType type, AlertSeverity severity, String title, String message, String actionUrl) {
        Notification notification = Notification.builder()
                .user(user).trip(trip).notificationType(type).severity(severity)
                .title(title).message(message).actionUrl(actionUrl).build();
        return notificationRepository.save(notification);
    }

    private String getSeverityIcon(AlertSeverity severity) {
        return switch (severity) {
            case LOW -> "ℹ️";
            case MEDIUM -> "⚠️";
            case HIGH -> "🔶";
            case CRITICAL -> "🚨";
        };
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .tripId(n.getTrip() != null ? n.getTrip().getId() : null)
                .tripTitle(n.getTrip() != null ? n.getTrip().getTitle() : null)
                .notificationType(n.getNotificationType().name())
                .severity(n.getSeverity().name())
                .severityIcon(getSeverityIcon(n.getSeverity()))
                .title(n.getTitle()).message(n.getMessage()).actionUrl(n.getActionUrl())
                .readStatus(n.getReadStatus()).dismissed(n.getDismissed())
                .scheduledFor(n.getScheduledFor()).createdAt(n.getCreatedAt()).build();
    }

    private NotificationPreferenceResponse mapPrefToResponse(NotificationPreference p) {
        return NotificationPreferenceResponse.builder()
                .id(p.getId())
                .departureReminders(p.getDepartureReminders()).checkinReminders(p.getCheckinReminders())
                .collaboratorAlerts(p.getCollaboratorAlerts()).budgetAlerts(p.getBudgetAlerts())
                .weatherAlerts(p.getWeatherAlerts()).quietHoursEnabled(p.getQuietHoursEnabled())
                .quietHoursStart(p.getQuietHoursStart() != null ? p.getQuietHoursStart().toString() : null)
                .quietHoursEnd(p.getQuietHoursEnd() != null ? p.getQuietHoursEnd().toString() : null)
                .budgetThresholdPercent(p.getBudgetThresholdPercent())
                .departureReminderHours(p.getDepartureReminderHours()).build();
    }
}
