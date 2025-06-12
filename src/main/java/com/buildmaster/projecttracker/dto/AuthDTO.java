package com.buildmaster.projecttracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDTO {
    public record RegisterUserRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email address")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min=8, message = "Password should be at least 8 characters long")
            String password
    ){}

    public record LoginUserRequest(
            @NotBlank(message = "Email is required")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ){}

    public record UserProfileResponse(
            Long id,
            String email,
            String role
    ){}

    public record JwtResponse(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String role
    ){}


}
