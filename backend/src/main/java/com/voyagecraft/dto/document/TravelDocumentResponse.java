package com.voyagecraft.dto.document;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TravelDocumentResponse {
    private Long id;
    private Long tripId;
    private String countryName;
    private String countryCode;
    private String visaRequirements;
    private String entryGuidelines;
    private String officialLink;
    private String additionalNotes;
    private List<ChecklistItemResponse> checklistItems;
    private List<ReminderResponse> reminders;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
