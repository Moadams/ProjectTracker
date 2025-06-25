package com.buildmaster.projecttracker.model;

import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "audit_logs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
