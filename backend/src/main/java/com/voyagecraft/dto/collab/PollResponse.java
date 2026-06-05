package com.voyagecraft.dto.collab;

import com.voyagecraft.enums.PollStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class PollResponse {
    private Long id;
    private Long tripId;
    private String question;
    private String category;
    private PollStatus status;
    private Boolean allowMultipleVotes;
    private String createdByName;
    private Long createdById;
    private List<PollOptionResponse> options;
    private int totalVotes;
    private LocalDateTime createdAt;
}
