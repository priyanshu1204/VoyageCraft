package com.voyagecraft.dto.notification;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long tripId;
    private String tripTitle;
    private String notificationType;
    private String severity;
    private String severityIcon;
    private String title;
    private String message;
    private String actionUrl;
    private Boolean readStatus;
    private Boolean dismissed;
    private LocalDateTime scheduledFor;
    private LocalDateTime createdAt;
}
