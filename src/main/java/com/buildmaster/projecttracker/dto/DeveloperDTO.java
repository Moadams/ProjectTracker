package com.buildmaster.projecttracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
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

    public record DeveloperResponse(
            Long id,
            String name,
            String email,
            String skills,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Set<TaskDTO.TaskSummaryResponse> assignedTasks
    ) {}

    public record DeveloperSummaryResponse(
            Long id,
            String name,
            String email
    ) {}
}
