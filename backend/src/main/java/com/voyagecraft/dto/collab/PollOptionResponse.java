package com.voyagecraft.dto.collab;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class PollOptionResponse {
    private Long id;
    private String optionText;
    private int voteCount;
    private double votePercent;
    private List<String> voterNames;
    private boolean currentUserVoted;
}
