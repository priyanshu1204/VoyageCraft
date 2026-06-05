package com.voyagecraft.dto.collaborator;

import com.voyagecraft.dto.auth.UserResponse;
import com.voyagecraft.enums.CollaboratorRole;
import com.voyagecraft.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CollaboratorResponse {
    private Long id;
    private UserResponse user;
    private CollaboratorRole role;
    private InvitationStatus invitationStatus;
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;
    private Long tripId;
    private String tripTitle;
    private String invitedByName;
}
