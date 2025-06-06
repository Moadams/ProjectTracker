package com.buildmaster.projecttracker.mapper;

import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.model.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {
    public Project toProjectEntity(ProjectDTO.ProjectRequest request) {
        return Project.builder()
                .name(request.name())
                .description(request.description())
                .deadLine(request.deadline())
                .build();
    }

    public ProjectDTO.ProjectResponse toProjectResponse(Project project) {
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
                project.getUpdatedAt(),
                project.getCreatedAt(),
                taskSummaries
        );
    }

    public ProjectDTO.ProjectSummaryResponse toProjectSummaryResponse(Project project) {
        return new ProjectDTO.ProjectSummaryResponse(
                project.getId(),
                project.getName(),
                project.getDeadLine(),
                project.getStatus()
        );
    }

    public void updateEntity(Project project, ProjectDTO.ProjectUpdateRequest request) {
        if (request.name() != null) project.setName(request.name());
        if (request.description() != null) project.setDescription(request.description());
        if (request.deadline() != null) project.setDeadLine(request.deadline());
        if (request.status() != null) project.setStatus(request.status());
    }

    private TaskDTO.TaskSummaryResponse toTaskSummaryResponse(Task task) {
        return new TaskDTO.TaskSummaryResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getDueDate()
        );
    }
}
