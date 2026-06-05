package com.voyagecraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "notification_preferences")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    private Boolean departureReminders = true;

    @Builder.Default
    private Boolean checkinReminders = true;

    @Builder.Default
    private Boolean collaboratorAlerts = true;

    @Builder.Default
    private Boolean budgetAlerts = true;

    @Builder.Default
    private Boolean weatherAlerts = true;

    @Builder.Default
    private Boolean quietHoursEnabled = false;

    private LocalTime quietHoursStart; // e.g. 22:00

    private LocalTime quietHoursEnd;   // e.g. 07:00

    @Builder.Default
    private Integer budgetThresholdPercent = 80; // alert when 80% of budget used

    @Builder.Default
    private Integer departureReminderHours = 24; // remind 24h before departure

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
