package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.mapper.ProjectMapper;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;


    @Transactional
    public ApiResponse<ProjectDTO.ProjectResponse> createProject(ProjectDTO.ProjectRequest projectRequest) {
        log.info("Creating new project: {}", projectRequest);
        Project project = projectMapper.toProjectEntity(projectRequest);
        Project savedProject = projectRepository.save(project);
        ProjectDTO.ProjectResponse response = projectMapper.toProjectResponse(savedProject);
        return ApiResponse.success("Project created", response);
    }

}
