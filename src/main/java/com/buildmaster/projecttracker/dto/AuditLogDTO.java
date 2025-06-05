package com.buildmaster.projecttracker.dto;

import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;

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
}
