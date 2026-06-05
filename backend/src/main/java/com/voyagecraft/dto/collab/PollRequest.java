package com.voyagecraft.dto.collab;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PollRequest {
    @NotNull private Long tripId;
    @NotBlank private String question;
    private String category; // "activity", "date", "stay", "transport"
    @NotNull private List<String> options;
    private Boolean allowMultipleVotes;
}
