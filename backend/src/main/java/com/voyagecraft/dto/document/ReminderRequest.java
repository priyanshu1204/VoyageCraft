package com.voyagecraft.dto.document;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReminderRequest {
    private String title;
    private String note;
    private String reminderDate; // ISO date string
}
