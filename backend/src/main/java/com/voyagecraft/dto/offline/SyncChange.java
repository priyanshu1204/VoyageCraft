package com.voyagecraft.dto.offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SyncChange {
    private String entityType;  // ITINERARY_ITEM, TRANSPORT, STAY, etc.
    private Long entityId;      // null for CREATE
    private String action;      // CREATE, UPDATE, DELETE
    private String payload;     // JSON string of the entity data
    private LocalDateTime timestamp; // when the change was made offline
}
