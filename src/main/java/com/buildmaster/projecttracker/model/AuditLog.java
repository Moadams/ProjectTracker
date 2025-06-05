package com.buildmaster.projecttracker.model;

import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "audit_logs")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuditLog {
    @Id
    private String id;

    @Indexed
    private ActionType actionType;

    @Indexed
    private EntityType entityType;

    @Indexed
    private String entityId;

    @Indexed
    private LocalDateTime timestamp;

    @Indexed
    private String actorName;

    @Indexed
    private String payload;

}
