package com.buildmaster.projecttracker.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

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
            Long projectId
    ) {}
}
