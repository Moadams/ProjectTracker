package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.DeveloperDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.mapper.DeveloperMapper;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final DeveloperMapper developerMapper;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public ApiResponse<Page<DeveloperDTO.DeveloperResponse>> getAllDevelopers(Pageable pageable) {
        Page<DeveloperDTO.DeveloperResponse> response = developerRepository.findAll(pageable).map(developerMapper::toDeveloperResponse);
        return ApiResponse.success("Developers List", response);
    }

    public ApiResponse<DeveloperDTO.DeveloperResponse> createDeveloper(DeveloperDTO.DeveloperRequest request) {
        Developer developer = developerMapper.toDeveloperEntity(request);
        Developer savedDeveloper = developerRepository.save(developer);
        DeveloperDTO.DeveloperResponse response = developerMapper.toDeveloperResponse(savedDeveloper);
        logAudit(ActionType.CREATE, EntityType.DEVELOPER, savedDeveloper.getId().toString(), "system", savedDeveloper);
        return ApiResponse.success("Developer created", response);
    }

    private void logAudit(ActionType actionType, EntityType entityType, String entityId, String actorName, Object entity) {
        try {
            String payload = (entity != null) ? objectMapper.writeValueAsString(entity) : null;
            auditLogService.logAction(actionType, entityType, entityId, actorName, payload);
        } catch (JsonProcessingException e) {
            System.err.println("Error converting entity to JSON for audit log: " + e.getMessage());
        }
    }
}
