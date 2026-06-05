package com.voyagecraft.dto.quickaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickNoteRequest {

    @NotNull
    private Long tripId;

    @NotBlank
    private String title;

    private String content;

    private String photoUrls; // comma-separated data URIs

    private String capturedLatitude;
    private String capturedLongitude;
    private String capturedLocationName;

    private Boolean isSynced;
    private Boolean isPinned;
}
