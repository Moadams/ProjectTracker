package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.AuditLogDTO;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.enums.TaskStatus;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.mapper.AuditLogMapper;
import com.buildmaster.projecttracker.mapper.TaskMapper;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.model.Task;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import com.buildmaster.projecttracker.repository.ProjectRepository;
import com.buildmaster.projecttracker.repository.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final TaskMapper taskMapper;
    private final AuditLogService auditLogService;



    public ApiResponse<Page<TaskDTO.TaskResponse>> getAllTasks(Pageable pageable) {
        Page<TaskDTO.TaskResponse> response = taskRepository.findAll(pageable).map(taskMapper::toTaskDTO);
        return ApiResponse.success("Tasks List", response);
    }

    @Transactional
    public ApiResponse<TaskDTO.TaskResponse> createTask(TaskDTO.TaskRequest request) {
        Task task = taskMapper.toTaskEntity(request);
        Task savedTask = taskRepository.save(task);
        TaskDTO.TaskResponse response = taskMapper.toTaskDTO(savedTask);
        auditLogService.logAudit(ActionType.CREATE, EntityType.TASK, savedTask.getId().toString(), "system", savedTask);
        return ApiResponse.success("Task created", response);
    }

    public ApiResponse<TaskDTO.TaskResponse> getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        TaskDTO.TaskResponse taskResponse = taskMapper.toTaskDTO(task);
        return ApiResponse.success("Task details", taskResponse);
    }

    public ApiResponse<List<TaskDTO.TaskSummaryResponse>> getTasksByProjectId(Long projectId) {
        List<TaskDTO.TaskSummaryResponse> response =  taskRepository.findByProjectId(projectId).stream()
                .map(taskMapper::toTaskSummaryResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Project tasks", response);
    }

    public ApiResponse<List<TaskDTO.TaskSummaryResponse>> getTasksByDeveloperId(Long developerId) {
        List<TaskDTO.TaskSummaryResponse> response =  taskRepository.findTasksByDeveloperId(developerId).stream()
                .map(taskMapper::toTaskSummaryResponse)
                .toList();
        return ApiResponse.success("Developer tasks", response);
    }

    public ApiResponse<TaskDTO.TaskResponse> updateTask(Long id, TaskDTO.TaskUpdateRequest taskRequest) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));


        if(taskRequest.title() != null) existingTask.setTitle(taskRequest.title());
        if(taskRequest.description() != null) existingTask.setDescription(taskRequest.description());
        if(taskRequest.dueDate() != null) existingTask.setDueDate(taskRequest.dueDate());
        if(taskRequest.projectId() != null){
            Project project = projectRepository.findById(taskRequest.projectId()).orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + taskRequest.projectId()));
            existingTask.setProject(project);
        }

        if(taskRequest.assignedDeveloperId() != null){
            Developer developer = developerRepository.findById(taskRequest.assignedDeveloperId()).orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + taskRequest.assignedDeveloperId()));
            existingTask.setAssignedDeveloper(developer);
        }

        Task updatedTask = taskRepository.save(existingTask);
        TaskDTO.TaskResponse response = taskMapper.toTaskDTO(updatedTask);
        auditLogService.logAudit(ActionType.UPDATE, EntityType.TASK, updatedTask.getId().toString(), "system", response);
        return ApiResponse.success("Task updated", response);
    }

    @Transactional
    public ApiResponse<TaskDTO.TaskResponse> updateTaskStatus(Long id, TaskDTO.TaskUpdateStatusRequest statusRequest) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        existingTask.setStatus(statusRequest.status());
        Task updatedTask = taskRepository.save(existingTask);

        TaskDTO.TaskResponse response = taskMapper.toTaskDTO(updatedTask);
        auditLogService.logAudit(ActionType.UPDATE, EntityType.TASK, updatedTask.getId().toString(), "system", response);
        return ApiResponse.success("Task status updated", response);
    }

    @Transactional
    public ApiResponse<Void> deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);
        auditLogService.logAudit(ActionType.UPDATE, EntityType.TASK, task.getId().toString(), "system", null);
        return ApiResponse.success("Developer Deleted", null);
    }

    public ApiResponse<List<TaskDTO.TaskSummaryResponse>> getOverdueTasks() {
        List<TaskDTO.TaskSummaryResponse> response = taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.COMPLETED).stream()
                .map(taskMapper::toTaskSummaryResponse)
                .toList();

        return ApiResponse.success("Tasks", response);
    }

    public ApiResponse<List<Object[]>> getTaskCountsByStatus() {
        List<Object[]> response = taskRepository.countTasksByStatus();
        return ApiResponse.success("Tasks", response);
    }


}
