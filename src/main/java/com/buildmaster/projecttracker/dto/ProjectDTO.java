package com.buildmaster.projecttracker.dto;

import com.buildmaster.projecttracker.enums.ProjectStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class ProjectDTO {
    public record ProjectRequest(
            @NotBlank(message = "Project name cannot be blank")
            @Size(max = 100, message = "Project name cannot exceed 100 characters")
            String name,
            @Size(max = 500, message = "Project description cannot exceed 500 characters")
            String description,
            @NotNull(message = "Deadline is required")
            @FutureOrPresent(message = "Deadline must be in the present or future")
            LocalDate deadline
    ) {}

    public record ProjectUpdateRequest(
       String name,
       String description,
       LocalDate deadline,
       ProjectStatus status
    ){}

    public record ProjectResponse(
            Long id,
            String name,
            String description,
            LocalDate deadline,
            ProjectStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Set<TaskDTO.TaskSummaryResponse> tasks
    ) {}

    public record ProjectSummaryResponse(
            Long id,
            String name,
            LocalDate deadline,
            ProjectStatus status
    ) {}

}
