package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Transactional
    public ProjectDTO.ProjectResponse createProject(ProjectDTO.ProjectRequest projectRequest) {
        log.info("Creating new project: {}", projectRequest);
        Project project = new Project();
        project.setName(projectRequest.name());
        project.setDescription(projectRequest.description());
        project.setDeadLine(projectRequest.deadline());
        Project savedProject = projectRepository.save(project);
        return mapToProjectResponse(savedProject);
    }

    private ProjectDTO.ProjectResponse mapToProjectResponse(Project project) {
        Set<TaskDTO.TaskSummaryResponse> taskSummaries = project.getTasks().stream()
                .map(task -> new TaskDTO.TaskSummaryResponse(
                        task.getId(),
                        task.getTitle(),
                        task.getStatus(),
                        task.getDueDate()
                )).collect(Collectors.toSet());

        return new ProjectDTO.ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getDeadLine(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                taskSummaries
        );
    }
}
