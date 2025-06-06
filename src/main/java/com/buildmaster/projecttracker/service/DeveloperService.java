package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.DeveloperDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.mapper.DeveloperMapper;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final DeveloperMapper developerMapper;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Cacheable(value="projects")
    public ApiResponse<Page<DeveloperDTO.DeveloperResponse>> getAllDevelopers(Pageable pageable) {
        Page<DeveloperDTO.DeveloperResponse> response = developerRepository.findAll(pageable).map(developerMapper::toDeveloperResponse);
        return ApiResponse.success("Developers List", response);
    }

    @Transactional
    public ApiResponse<DeveloperDTO.DeveloperResponse> createDeveloper(DeveloperDTO.DeveloperRequest request) {
        Developer developer = developerMapper.toDeveloperEntity(request);
        Developer savedDeveloper = developerRepository.save(developer);
        DeveloperDTO.DeveloperResponse response = developerMapper.toDeveloperResponse(savedDeveloper);
        logAudit(ActionType.CREATE, EntityType.DEVELOPER, savedDeveloper.getId().toString(), "system", savedDeveloper);
        return ApiResponse.success("Developer created", response);
    }

    @Cacheable(value="developers", key="#id")
    public ApiResponse<DeveloperDTO.DeveloperResponse> getDeleveloperById(Long id){
        Developer developer = developerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + id));
        DeveloperDTO.DeveloperResponse response = developerMapper.toDeveloperResponse(developer);
        return ApiResponse.success("Developer details", response);
    }


    @Transactional
    @CacheEvict(value="developers", key="#id")
    public ApiResponse<DeveloperDTO.DeveloperResponse> updateDeveloper(Long id, DeveloperDTO.DeveloperRequest request) {
        Developer developer = developerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + id));
        developerMapper.updateEntity(developer, request);
        Developer updatedDeveloper = developerRepository.save(developer);
        DeveloperDTO.DeveloperResponse response = developerMapper.toDeveloperResponse(updatedDeveloper);
        return ApiResponse.success("Developer updated", response);
    }

    @Transactional
    public ApiResponse<Void> deleteDeveloper(Long id) {
        Developer developer = developerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + id));
        developerRepository.delete(developer);
        return ApiResponse.success("Developer deleted", null);
    }

    @Cacheable(value = "developers")
    public ApiResponse<List<DeveloperDTO.DeveloperSummaryResponse>> getTop5DevelopersWithMostTasks() {
        List<DeveloperDTO.DeveloperSummaryResponse> developers =  developerRepository.findTop5DevelopersWithMostTasks().stream()
                .map(obj -> new DeveloperDTO.DeveloperSummaryResponse(
                        (Long) obj[0],
                        (String) obj[1],
                        (String) obj[2]
                )).collect(Collectors.toList());
        return ApiResponse.success("Top 5 developers with most tasks", developers);
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
