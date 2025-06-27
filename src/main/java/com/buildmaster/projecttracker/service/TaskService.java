package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.enums.TaskStatus;
import com.buildmaster.projecttracker.event.TaskAssignedEvent;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.mapper.TaskMapper;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.model.Task;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import com.buildmaster.projecttracker.repository.ProjectRepository;
import com.buildmaster.projecttracker.repository.TaskRepository;
import com.buildmaster.projecttracker.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NotificationService notificationService;


    @Cacheable(
            value = "allTasks"
    )
    public CustomApiResponse<Page<TaskDTO.TaskResponse>> getAllTasks(Pageable pageable) {
        Page<TaskDTO.TaskResponse> response = taskRepository.findAll(pageable).map(taskMapper::toTaskDTO);
        return CustomApiResponse.success("Tasks List", response);
    }

    @CacheEvict(value = {"allProjects", "projectsWithoutTasks", "allTasks","topDevelopers"}, allEntries = true)
    @Transactional
    public CustomApiResponse<TaskDTO.TaskResponse> createTask(TaskDTO.TaskRequest request) {
        Task task = taskMapper.toTaskEntity(request);
        Task savedTask = taskRepository.save(task);
        TaskDTO.TaskResponse response = taskMapper.toTaskDTO(savedTask);
        auditLogService.logAudit(ActionType.CREATE, EntityType.TASK, savedTask.getId().toString(), "system", response);


        if(savedTask.getAssignedDeveloper() != null) {
            notificationService.createNotification(
                    savedTask.getAssignedDeveloper().getEmail(),
                    "You have been assigned to task: " + savedTask.getTitle() ,
                    "TASK_ASSIGNMENT",
                    savedTask.getId(),
                    Task.class.getSimpleName()
            );
            applicationEventPublisher.publishEvent(new TaskAssignedEvent(this, savedTask, savedTask.getAssignedDeveloper()));
        }
        return CustomApiResponse.success("Task created", response);
    }

    @Cacheable(value = "tasks", key="#id")
    public CustomApiResponse<TaskDTO.TaskResponse> getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        TaskDTO.TaskResponse taskResponse = taskMapper.toTaskDTO(task);
        return CustomApiResponse.success("Task details", taskResponse);
    }

    public CustomApiResponse<List<TaskDTO.TaskSummaryResponse>> getTasksByProjectId(Long projectId) {
        List<TaskDTO.TaskSummaryResponse> response =  taskRepository.findByProjectId(projectId).stream()
                .map(taskMapper::toTaskSummaryResponse)
                .collect(Collectors.toList());

        return CustomApiResponse.success("Project tasks", response);
    }

    public CustomApiResponse<List<TaskDTO.TaskSummaryResponse>> getTasksByDeveloperId(Long developerId) {
        List<TaskDTO.TaskSummaryResponse> response =  taskRepository.findTasksByDeveloperId(developerId).stream()
                .map(taskMapper::toTaskSummaryResponse)
                .toList();
        return CustomApiResponse.success("Developer tasks", response);
    }

    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#id"),
            @CacheEvict(value = {"allTasks","overdueTasks", "taskCounts"}, allEntries = true)
    })
    public CustomApiResponse<TaskDTO.TaskResponse> updateTask(Long id, TaskDTO.TaskUpdateRequest taskRequest) {
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
        return CustomApiResponse.success("Task updated", response);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value="tasks", key="#id"),
            @CacheEvict(value = {"allTasks", "taskCounts", "overdueTasks"}, allEntries = true)
    })
    public CustomApiResponse<TaskDTO.TaskResponse> updateTaskStatus(Long id, TaskDTO.TaskUpdateStatusRequest statusRequest) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        existingTask.setStatus(statusRequest.status());
        Task updatedTask = taskRepository.save(existingTask);

        TaskDTO.TaskResponse response = taskMapper.toTaskDTO(updatedTask);
        auditLogService.logAudit(ActionType.UPDATE, EntityType.TASK, updatedTask.getId().toString(), "system", response);
        if(updatedTask.getAssignedDeveloper() != null){
                notificationService.createNotification(
                        updatedTask.getAssignedDeveloper().getEmail(),
                        "Status of task '" + updatedTask.getTitle() + " in project ",
                        "TASK_STATUS_UPDATE",
                        updatedTask.getId(),
                        Task.class.getSimpleName()
                );
        }
        return CustomApiResponse.success("Task status updated", response);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value="tasks", key="#id"),
            @CacheEvict(value = {"allTasks","taskCounts","overdueTasks"}, allEntries = true)
    })
    public CustomApiResponse<Void> deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);
        auditLogService.logAudit(ActionType.UPDATE, EntityType.TASK, task.getId().toString(), "system", null);
        return CustomApiResponse.success("Developer Deleted", null);
    }

    @Cacheable(value="overdueTasks")
    public CustomApiResponse<List<TaskDTO.TaskSummaryResponse>> getOverdueTasks() {
        List<TaskDTO.TaskSummaryResponse> response = taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.COMPLETED).stream()
                .map(taskMapper::toTaskSummaryResponse)
                .toList();

        return CustomApiResponse.success("Tasks", response);
    }

    @Cacheable(value="taskCounts")
    public CustomApiResponse<List<Object[]>> getTaskCountsByStatus() {
        List<Object[]> response = taskRepository.countTasksByStatus();
        return CustomApiResponse.success("Tasks", response);
    }


}
