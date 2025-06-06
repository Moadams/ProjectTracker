package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.AuditLogDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.mapper.AuditLogMapper;
import com.buildmaster.projecttracker.model.AuditLog;
import com.buildmaster.projecttracker.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    @Async
    public void logAction(ActionType actionType, EntityType entityType, String entityId, String actorName, String payload) {
        AuditLog auditLog = AuditLog.builder()
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .timestamp(LocalDateTime.now())
                .actorName(actorName)
                .payload(payload)
                .build();
        auditLogRepository.save(auditLog);
    }

    public void logAudit(ActionType actionType, EntityType entityType, String entityId, String actorName, Object entity) {
        try {
            String payload = (entity != null) ? objectMapper.writeValueAsString(entity) : null;
            logAction(actionType, entityType, entityId, actorName, payload);
        } catch (JsonProcessingException e) {
            // Log error or handle appropriately
            System.err.println("Error converting entity to JSON for audit log: " + e.getMessage());
        }
    }

    public List<AuditLogDTO.AuditLogResponse> getLogsByEntityType(EntityType entityType) {
        return auditLogRepository.findByEntityType(entityType).stream()
                .map(auditLogMapper::toAuditLogDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO.AuditLogResponse> getLogsByEntityTypeAndActorName(EntityType entityType, String actorName) {
        return auditLogRepository.findByEntityTypeAndActorName(entityType, actorName).stream()
                .map(auditLogMapper::toAuditLogDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO.AuditLogResponse> getLogsByActorName(String actorName) {
        return auditLogRepository.findByActorName(actorName).stream()
                .map(auditLogMapper::toAuditLogDTO)
                .collect(Collectors.toList());
    }
}
