package com.voyagecraft.dto.offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SyncRequest {
    private Long tripId;
    private Long clientVersion;
    private LocalDateTime lastSyncedAt;
    private List<SyncChange> changes;
    private Boolean reducedMode;
}
