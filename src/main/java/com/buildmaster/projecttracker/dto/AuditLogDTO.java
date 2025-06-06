package com.buildmaster.projecttracker.dto;

import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AuditLogDTO {
    public record AuditLogResponse(
            String id,
            ActionType actionType,
            EntityType entityType,
            String entityId,
            LocalDateTime timestamp,
            String actorName,
            String payload // JSON string
    ) {}

    public record AuditTaskInfo(
            Long id,
            String title,
            String description,
            LocalDate dueDate,
            Long projectId,
            String projectName,
            Long assignedDeveloperId,
            String assignedDeveloperName
    ) {
        public static AuditTaskInfo from(Task task) {
            return new AuditTaskInfo(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getDueDate(),
                    task.getProject() != null ? task.getProject().getId() : null,
                    task.getProject() != null ? task.getProject().getName() : null,
                    task.getAssignedDeveloper() != null ? task.getAssignedDeveloper().getId() : null,
                    task.getAssignedDeveloper() != null ? task.getAssignedDeveloper().getName() : null
            );
        }
    }

}
