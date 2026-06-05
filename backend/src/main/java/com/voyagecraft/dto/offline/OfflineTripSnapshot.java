package com.voyagecraft.dto.offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OfflineTripSnapshot {
    private Long tripId;
    private String tripTitle;
    private Long versionNumber;
    private LocalDateTime lastSyncedAt;
    private Boolean reducedMode;
    private long dataSizeBytes;
}
