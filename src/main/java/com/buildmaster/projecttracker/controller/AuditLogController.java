package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.AuditLogDTO;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLogDTO.AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) String actorName) {

        List<AuditLogDTO.AuditLogResponse> logs;
        if (entityType != null && actorName != null) {
            logs = auditLogService.getLogsByEntityTypeAndActorName(entityType, actorName);
        } else if (entityType != null) {
            logs = auditLogService.getLogsByEntityType(entityType);
        } else if (actorName != null) {
            logs = auditLogService.getLogsByActorName(actorName);
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(logs);
    }
}
