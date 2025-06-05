package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.AuditLogDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.mapper.AuditLogMapper;
import com.buildmaster.projecttracker.model.AuditLog;
import com.buildmaster.projecttracker.repository.AuditLogRepository;
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
