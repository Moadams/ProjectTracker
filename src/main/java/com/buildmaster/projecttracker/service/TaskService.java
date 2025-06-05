package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.mapper.TaskMapper;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.model.Task;
import com.buildmaster.projecttracker.repository.ProjectRepository;
import com.buildmaster.projecttracker.repository.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;


    public ApiResponse<Page<TaskDTO.TaskResponse>> getAllTasks(Pageable pageable) {
        Page<TaskDTO.TaskResponse> response = taskRepository.findAll(pageable).map(taskMapper::toTaskDTO);
        return ApiResponse.success("Tasks List", response);
    }

    @Transactional
    public ApiResponse<TaskDTO.TaskResponse> createTask(TaskDTO.TaskRequest request) {
        Task task = taskMapper.toTaskEntity(request);
        Task savedTask = taskRepository.save(task);
        TaskDTO.TaskResponse response = taskMapper.toTaskDTO(savedTask);
        logAudit(ActionType.CREATE, EntityType.TASK, savedTask.getId().toString(), "system", savedTask);
        return ApiResponse.success("Task created", response);
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
