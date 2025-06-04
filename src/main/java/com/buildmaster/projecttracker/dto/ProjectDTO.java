package com.buildmaster.projecttracker.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

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

}
