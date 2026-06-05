package com.voyagecraft.dto.collab;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull private Long tripId;
    @NotBlank private String itemType;
    @NotNull private Long itemId;
    @NotBlank private String content;
    private Long parentId;
}
