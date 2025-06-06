package com.buildmaster.projecttracker.mapper;

import com.buildmaster.projecttracker.dto.DeveloperDTO;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.model.Task;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class DeveloperMapper {
    public Developer toDeveloperEntity(DeveloperDTO.DeveloperRequest request){
        return Developer.builder()
                .name(request.name())
                .email(request.email())
                .skills(request.skills())
                .build();
    }

    public DeveloperDTO.DeveloperResponse toDeveloperResponse(Developer developer){

        return new DeveloperDTO.DeveloperResponse(
                developer.getId(),
                developer.getName(),
                developer.getEmail(),
                developer.getSkills(),
                developer.getCreatedAt(),
                developer.getUpdatedAt(),
                developer.getAssignedTasks().stream().map(this::toTaskSummaryResponse).collect(Collectors.toSet())
        );
    }

    public void updateEntity(Developer developer, DeveloperDTO.DeveloperRequest request){
        if(request.name() != null) developer.setName(request.name());
        if(request.email() != null) developer.setEmail(request.email());
        if (request.skills() != null) developer.setSkills(new HashSet<>(request.skills()));
    }

    private TaskDTO.TaskSummaryResponse toTaskSummaryResponse(Task task){
        return new TaskDTO.TaskSummaryResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getDueDate()
        );
    }
}
