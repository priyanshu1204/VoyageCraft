package com.voyagecraft.dto.notification;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationPreferenceResponse {
    private Long id;
    private Boolean departureReminders;
    private Boolean checkinReminders;
    private Boolean collaboratorAlerts;
    private Boolean budgetAlerts;
    private Boolean weatherAlerts;
    private Boolean quietHoursEnabled;
    private String quietHoursStart;
    private String quietHoursEnd;
    private Integer budgetThresholdPercent;
    private Integer departureReminderHours;
}
