package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.enums.ProjectStatus;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.mapper.ProjectMapper;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;


    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ApiResponse<ProjectDTO.ProjectResponse> createProject(ProjectDTO.ProjectRequest projectRequest) {
        log.info("Creating new project: {}", projectRequest);
        Project project = projectMapper.toProjectEntity(projectRequest);
        Project savedProject = projectRepository.save(project);
        ProjectDTO.ProjectResponse response = projectMapper.toProjectResponse(savedProject);
        return ApiResponse.success("Project created", response);
    }

    @Transactional
    @Cacheable(value = "allProjects")
    public ApiResponse<Page<ProjectDTO.ProjectResponse>> getAllProjects(Pageable pageable) {
        Page<ProjectDTO.ProjectResponse> response =  projectRepository.findAll(pageable)
                .map(projectMapper::toProjectResponse);
        return ApiResponse.success("Project list", response);

    }


    @Cacheable(value="projects", key="#id")
    public ApiResponse<ProjectDTO.ProjectResponse> getProjectById(Long id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        ProjectDTO.ProjectResponse response = projectMapper.toProjectResponse(project);
        return ApiResponse.success("Project details", response);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"), // Evict specific project cache
            @CacheEvict(value = {"allProjects", "projectsWithoutTasks"}, allEntries = true) // Evict relevant lists
    })
    public ApiResponse<ProjectDTO.ProjectResponse> updateProject(Long id, ProjectDTO.ProjectUpdateRequest projectRequest) {
        Project existingProject = projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectMapper.updateEntity(existingProject, projectRequest);
        Project updatedProject = projectRepository.save(existingProject);
        ProjectDTO.ProjectResponse response = projectMapper.toProjectResponse(updatedProject);
        return ApiResponse.success("Project updated", response);
    }

    @Transactional
    @CacheEvict(value="projects", key="#id")
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"), // Evict specific project cache
            @CacheEvict(value = {"allProjects", "projectsWithoutTasks"}, allEntries = true) // Evict relevant lists
    })
    public ApiResponse<Void> deleteProject(Long id) {
        Project existingProject = projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(existingProject);
        return ApiResponse.success("Project deleted", null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ProjectDTO.ProjectResponse>> getOverdueProjects() {
        log.info("Fetching overdue projects");

        List<Project> overdueProjects = projectRepository.findOverdueProjects(
                LocalDate.now(), ProjectStatus.ACTIVE);

        List<ProjectDTO.ProjectResponse> responses = overdueProjects.stream()
                .map(projectMapper::toProjectResponse)
                .toList();

        return ApiResponse.success(responses);
    }

    @Cacheable(value = "projectsWithoutTasks")
    public ApiResponse<List<ProjectDTO.ProjectSummaryResponse>> getProjectsWithoutTasks() {
        List<ProjectDTO.ProjectSummaryResponse> responses = projectRepository.findProjectsWithoutTasks().stream()
                .map(projectMapper::toProjectSummaryResponse)
                .toList();

        return ApiResponse.success(responses);
    }

}
