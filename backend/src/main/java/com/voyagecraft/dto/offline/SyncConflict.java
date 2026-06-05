package com.voyagecraft.dto.offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SyncConflict {
    private String entityType;
    private Long entityId;
    private String clientData;   // what the client tried to set
    private String serverData;   // what the server currently has
    private String message;      // human-readable conflict description
}
