package com.voyagecraft.dto.document;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChecklistItemResponse {
    private Long id;
    private String itemName;
    private String itemType;
    private String description;
    private String documentLink;
    private Boolean completed;
}
