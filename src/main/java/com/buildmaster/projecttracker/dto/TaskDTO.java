package com.buildmaster.projecttracker.dto;

import com.buildmaster.projecttracker.enums.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class TaskDTO {
    public record TaskRequest(
            @NotBlank(message = "Task title cannot be blank")
            @Size(max = 200, message = "Task title cannot exceed 200 characters")
            String title,
            @Size(max = 1000, message = "Task description cannot exceed 1000 characters")
            String description,
            @NotNull(message = "Due date is required")
            @FutureOrPresent(message = "Due date must be in the present or future")
            LocalDate dueDate,
            @NotNull(message = "Project ID is required")
            Long projectId,
            Long assignedDeveloperId
    ) {}

    public record TaskUpdateRequest(
            @Size(max = 200, message = "Task title cannot exceed 200 characters")
            String title,
            @Size(max = 1000, message = "Task description cannot exceed 1000 characters")
            String description,
            @FutureOrPresent(message = "Due date must be in the present or future")
            LocalDate dueDate,
            Long projectId,
            Long assignedDeveloperId
    ){}

    public record TaskUpdateStatusRequest(
            @NotNull(message = "Status is required")
            TaskStatus status
    ) {}

    public record TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            LocalDate dueDate,
            boolean overdue,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            DeveloperDTO.DeveloperSummaryResponse assignedDeveloper,
            ProjectDTO.ProjectSummaryResponse project
    ) {}

    public record TaskSummaryResponse(
            Long id,
            String title,
            TaskStatus status,
            LocalDate dueDate
    ) {}
}
