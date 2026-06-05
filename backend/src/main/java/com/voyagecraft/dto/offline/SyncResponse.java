package com.voyagecraft.dto.offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SyncResponse {
    private Long serverVersion;
    private LocalDateTime syncedAt;
    private String snapshotData;       // Full trip data JSON for client cache
    private List<SyncConflict> conflicts;
    private int appliedChanges;
    private int rejectedChanges;
    private String syncStatusMessage;
}
