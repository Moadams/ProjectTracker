package com.buildmaster.projecttracker.mapper;

import com.buildmaster.projecttracker.dto.DeveloperDTO;
import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.model.Task;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import com.buildmaster.projecttracker.repository.ProjectRepository;
import com.buildmaster.projecttracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMapper {
    
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    
    public Task toTaskEntity(TaskDTO.TaskRequest request){
        Project project = projectRepository.findById(request.projectId()).orElseThrow(()-> new ResourceNotFoundException("Project not found with id " + request.projectId()));

        Developer assignedDeveloper = null;
        if (request.assignedDeveloperId() != null) {
            assignedDeveloper = developerRepository.findById(request.assignedDeveloperId())
                    .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + request.assignedDeveloperId()));
        }

        return Task.builder()
                .title(request.title())
                .description(request.description())
                .project(project)
                .dueDate(request.dueDate())
                .assignedDeveloper(assignedDeveloper)
                .build();
    }

    public TaskDTO.TaskResponse toTaskDTO(Task task){

        DeveloperDTO.DeveloperSummaryResponse assignedDeveloper = new DeveloperDTO.DeveloperSummaryResponse(
                1L, "test", "new@gmail.com"
        );
        ProjectDTO.ProjectSummaryResponse project = new ProjectDTO.ProjectSummaryResponse(
                task.getProject().getId(),
                task.getProject().getName(),
                task.getProject().getDeadLine(),
                task.getProject().getStatus()
        );
        return new TaskDTO.TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.isOverdue(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                assignedDeveloper,
                project
        );
    }
}
