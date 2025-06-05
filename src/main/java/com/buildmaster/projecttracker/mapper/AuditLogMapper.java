package com.buildmaster.projecttracker.mapper;

import com.buildmaster.projecttracker.dto.AuditLogDTO;
import com.buildmaster.projecttracker.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditLogMapper {
    public AuditLogDTO.AuditLogResponse toAuditLogDTO(AuditLog auditLog) {
        return new AuditLogDTO.AuditLogResponse(
                auditLog.getId(),
                auditLog.getActionType(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getTimestamp(),
                auditLog.getActorName(),
                auditLog.getPayload()
        );
    }
}
