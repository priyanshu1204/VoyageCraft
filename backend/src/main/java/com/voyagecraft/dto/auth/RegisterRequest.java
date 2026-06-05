package com.voyagecraft.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Please provide a valid first name")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Please provide a valid last name")
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank(message = "Please provide a valid email")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Please provide a valid password")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}
