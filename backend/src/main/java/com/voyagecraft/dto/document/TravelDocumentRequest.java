package com.voyagecraft.dto.document;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TravelDocumentRequest {
    private String countryName;
    private String countryCode;
    private String visaRequirements;
    private String entryGuidelines;
    private String officialLink;
    private String additionalNotes;
    private List<ChecklistItemRequest> checklistItems;
    private List<ReminderRequest> reminders;
}
