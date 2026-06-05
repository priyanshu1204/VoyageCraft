package com.voyagecraft.dto.collab;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class CommentResponse {
    private Long id;
    private Long tripId;
    private String itemType;
    private Long itemId;
    private String content;
    private String authorName;
    private Long authorId;
    private Long parentId;
    private List<CommentResponse> replies;
    private LocalDateTime createdAt;
}
