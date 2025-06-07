package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.dto.DeveloperDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.mapper.DeveloperMapper;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Cacheable(value="projects")
    public CustomApiResponse<Page<DeveloperDTO.DeveloperSummaryResponse>> getAllDevelopers(Pageable pageable) {
        Page<DeveloperDTO.DeveloperSummaryResponse> response = developerRepository.findAll(pageable).map(developerMapper::toDeveloperSummaryResponse);
        return CustomApiResponse.success("Developers List", response);
    }

    @Transactional
    public CustomApiResponse<DeveloperDTO.DeveloperResponse> createDeveloper(DeveloperDTO.DeveloperRequest request) {
        Developer developer = developerMapper.toDeveloperEntity(request);
        Developer savedDeveloper = developerRepository.save(developer);
        DeveloperDTO.DeveloperResponse response = developerMapper.toDeveloperResponse(savedDeveloper);
        auditLogService.logAudit(ActionType.CREATE, EntityType.DEVELOPER, savedDeveloper.getId().toString(), "system", savedDeveloper);
        return CustomApiResponse.success("Developer created", response);
    }

    @Cacheable(value="developers", key="#id")
    public CustomApiResponse<DeveloperDTO.DeveloperResponse> getDeleveloperById(Long id){
        Developer developer = developerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + id));
        DeveloperDTO.DeveloperResponse response = developerMapper.toDeveloperResponse(developer);
        return CustomApiResponse.success("Developer details", response);
    }


    @Transactional
    @CacheEvict(value="developers", key="#id")
    public CustomApiResponse<DeveloperDTO.DeveloperResponse> updateDeveloper(Long id, DeveloperDTO.DeveloperRequest request) {
        Developer developer = developerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + id));
        developerMapper.updateEntity(developer, request);
        Developer updatedDeveloper = developerRepository.save(developer);
        DeveloperDTO.DeveloperResponse response = developerMapper.toDeveloperResponse(updatedDeveloper);
        auditLogService.logAudit(ActionType.UPDATE, EntityType.DEVELOPER, updatedDeveloper.getId().toString(), "system", response);
        return CustomApiResponse.success("Developer updated", response);
    }

    @Transactional
    public CustomApiResponse<Void> deleteDeveloper(Long id) {
        Developer developer = developerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + id));
        developerRepository.delete(developer);
        auditLogService.logAudit(ActionType.UPDATE, EntityType.DEVELOPER, developer.getId().toString(), "system", null);
        return CustomApiResponse.success("Developer deleted", null);
    }

    @Cacheable(value = "developers")
    public CustomApiResponse<List<DeveloperDTO.DeveloperSummaryResponse>> getTop5DevelopersWithMostTasks() {
        List<DeveloperDTO.DeveloperSummaryResponse> developers =  developerRepository.findTop5DevelopersWithMostTasks().stream()
                .map(obj -> new DeveloperDTO.DeveloperSummaryResponse(
                        (Long) obj[0],
                        (String) obj[1],
                        (String) obj[2]
                )).collect(Collectors.toList());
        return CustomApiResponse.success("Top 5 developers with most tasks", developers);
    }


}
