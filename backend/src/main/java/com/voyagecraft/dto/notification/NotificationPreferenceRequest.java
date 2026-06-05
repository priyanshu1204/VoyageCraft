package com.voyagecraft.dto.notification;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationPreferenceRequest {
    private Boolean departureReminders;
    private Boolean checkinReminders;
    private Boolean collaboratorAlerts;
    private Boolean budgetAlerts;
    private Boolean weatherAlerts;
    private Boolean quietHoursEnabled;
    private String quietHoursStart; // "22:00"
    private String quietHoursEnd;   // "07:00"
    private Integer budgetThresholdPercent;
    private Integer departureReminderHours;
}
