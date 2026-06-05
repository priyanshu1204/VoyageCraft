package com.voyagecraft.dto.collaborator;

import com.voyagecraft.enums.CollaboratorRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CollaboratorRequest {

    @NotBlank(message = "Please provide a valid email")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotNull(message = "Please provide a valid role")
    private CollaboratorRole role;
}
