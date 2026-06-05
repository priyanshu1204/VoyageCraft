package com.voyagecraft.dto.document;

import lombok.*;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReminderResponse {
    private Long id;
    private String title;
    private String note;
    private LocalDate reminderDate;
    private Boolean dismissed;
}
