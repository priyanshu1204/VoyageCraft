package com.voyagecraft.dto.quickaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickNoteResponse {
    private Long id;
    private Long tripId;
    private String title;
    private String content;
    private String photoUrls;
    private String capturedLatitude;
    private String capturedLongitude;
    private String capturedLocationName;
    private Boolean isSynced;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
