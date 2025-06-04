package com.buildmaster.projecttracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class DeveloperDTO {
    public record DeveloperRequest(
            @NotBlank(message = "Developer name cannot be blank")
            @Size(max = 100, message = "Developer name cannot exceed 100 characters")
            String name,
            @NotBlank(message = "Email cannot be blank")
            @Email(message = "Invalid email format")
            String email,
            Set<String> skills
    ) {}
}
