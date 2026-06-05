package com.voyagecraft.dto.collab;

import com.voyagecraft.enums.ChangeAction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class ChangeLogResponse {
    private Long id;
    private ChangeAction action;
    private String entityType;
    private Long entityId;
    private String description;
    private String userName;
    private Long userId;
    private LocalDateTime createdAt;
}
