package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.enums.ProjectStatus;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.mapper.ProjectMapper;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.model.Task;
import com.buildmaster.projecttracker.repository.ProjectRepository;
import com.buildmaster.projecttracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final AuditLogService auditLogService;
    private final TaskRepository taskRepository;


    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public CustomApiResponse<ProjectDTO.ProjectResponse> createProject(ProjectDTO.ProjectRequest projectRequest) {
        log.info("Creating new project: {}", projectRequest);
        Project project = projectMapper.toProjectEntity(projectRequest);
        Project savedProject = projectRepository.save(project);
        ProjectDTO.ProjectResponse response = projectMapper.toProjectResponse(savedProject);
        auditLogService.logAudit(ActionType.CREATE, EntityType.PROJECT, savedProject.getId().toString(), "system", savedProject);
        return CustomApiResponse.success("Project created", response);
    }

    @Transactional
    @Cacheable(value = "allProjects")
    public CustomApiResponse<Page<ProjectDTO.ProjectResponse>> getAllProjects(Pageable pageable) {
        Page<ProjectDTO.ProjectResponse> response =  projectRepository.findAll(pageable)
                .map(projectMapper::toProjectResponse);
        return CustomApiResponse.success("Project list", response);

    }


    @Cacheable(value="projects", key="#id")
    public CustomApiResponse<ProjectDTO.ProjectResponse> getProjectById(Long id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        ProjectDTO.ProjectResponse response = projectMapper.toProjectResponse(project);
        return CustomApiResponse.success("Project details", response);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"), // Evict specific project cache
            @CacheEvict(value = {"allProjects", "projectsWithoutTasks"}, allEntries = true) // Evict relevant lists
    })
    public CustomApiResponse<ProjectDTO.ProjectResponse> updateProject(Long id, ProjectDTO.ProjectUpdateRequest projectRequest) {
        Project existingProject = projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectMapper.updateEntity(existingProject, projectRequest);
        Project updatedProject = projectRepository.save(existingProject);
        ProjectDTO.ProjectResponse response = projectMapper.toProjectResponse(updatedProject);
        auditLogService.logAudit(ActionType.UPDATE, EntityType.PROJECT, updatedProject.getId().toString(), "system", updatedProject);
        return CustomApiResponse.success("Project updated", response);
    }

    @Transactional
    @CacheEvict(value="projects", key="#id")
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"), // Evict specific project cache
            @CacheEvict(value = {"allProjects", "projectsWithoutTasks"}, allEntries = true) // Evict relevant lists
    })
    public CustomApiResponse<Void> deleteProject(Long id) {
        Project existingProject = projectRepository.findWithTasksById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        System.out.println("Task count: " + existingProject.getTasks().size());

        List<Task> projectTasks = taskRepository.findByProjectId(id);

        if (projectTasks != null && !projectTasks.isEmpty()) {
            projectTasks.forEach(task -> task.setProject(null));
            projectTasks.forEach(task -> taskRepository.deleteById(task.getId()));
            existingProject.getTasks().clear();
        }

        projectRepository.delete(existingProject);
        auditLogService.logAudit(ActionType.DELETE, EntityType.PROJECT, id.toString(), "system", null);
        return CustomApiResponse.success("Project deleted", null);
    }

    @Transactional(readOnly = true)
    public CustomApiResponse<List<ProjectDTO.ProjectResponse>> getOverdueProjects() {
        log.info("Fetching overdue projects");

        List<Project> overdueProjects = projectRepository.findOverdueProjects(
                LocalDate.now(), ProjectStatus.ACTIVE);

        List<ProjectDTO.ProjectResponse> responses = overdueProjects.stream()
                .map(projectMapper::toProjectResponse)
                .toList();

        return CustomApiResponse.success(responses);
    }

    @Cacheable(value = "projectsWithoutTasks")
    public CustomApiResponse<List<ProjectDTO.ProjectSummaryResponse>> getProjectsWithoutTasks() {
        List<ProjectDTO.ProjectSummaryResponse> responses = projectRepository.findProjectsWithoutTasks().stream()
                .map(projectMapper::toProjectSummaryResponse)
                .toList();

        return CustomApiResponse.success(responses);
    }

}
