package com.voyagecraft.dto.document;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChecklistItemRequest {
    private String itemName;
    private String itemType; // VISA, VACCINE, FORM, INSURANCE, PASSPORT, PERMIT, OTHER
    private String description;
    private String documentLink;
    private Boolean completed;
}
