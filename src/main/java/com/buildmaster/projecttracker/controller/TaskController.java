package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Task Management", description = "Operations related to tasks")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Get all tasks with pagination and sorting",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Number of records per page", example = "10"),
                    @Parameter(name = "sort", description = "Sort order (field,asc/desc)", example = "dueDate,asc")
            },
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved list of tasks"))
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<CustomApiResponse<Page<TaskDTO.TaskResponse>>> getTasks(Pageable pageable) {
        CustomApiResponse<Page<TaskDTO.TaskResponse>> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @Operation(summary = "Create a new task",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Task created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid task data"),
                    @ApiResponse(responseCode = "404", description = "Project or Developer not found")
            })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<CustomApiResponse<TaskDTO.TaskResponse>> createTask(@Valid @RequestBody TaskDTO.TaskRequest request){
        CustomApiResponse<TaskDTO.TaskResponse> createdTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @Operation(summary = "Get a task by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task found"),
                    @ApiResponse(responseCode = "404", description = "Task not found")
            })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @taskSecurity.isTaskOwner(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<CustomApiResponse<TaskDTO.TaskResponse>> getTaskById(@PathVariable Long id) {
        CustomApiResponse<TaskDTO.TaskResponse> task = taskService.getTaskById(id);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @Operation(summary = "Get tasks by Project ID",
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks for the project"))
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<CustomApiResponse<List<TaskDTO.TaskSummaryResponse>>> getTasksByProjectId(@PathVariable Long projectId) {
        CustomApiResponse<List<TaskDTO.TaskSummaryResponse>> tasks = taskService.getTasksByProjectId(projectId);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @Operation(summary = "Get tasks by Developer ID",
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks assigned to the developer"))
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/developer/{developerId}")
    public ResponseEntity<CustomApiResponse<List<TaskDTO.TaskSummaryResponse>>> getTasksByDeveloperId(@PathVariable Long developerId) {
        CustomApiResponse<List<TaskDTO.TaskSummaryResponse>> tasks = taskService.getTasksByDeveloperId(developerId);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @Operation(summary = "Update an existing task",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid task data"),
                    @ApiResponse(responseCode = "404", description = "Task, Project, or Developer not found")
            })
    @PreAuthorize("@taskSecurity.isTaskOwner(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<CustomApiResponse<TaskDTO.TaskResponse>> updateTask(@Valid @PathVariable Long id, @Valid @RequestBody TaskDTO.TaskUpdateRequest taskRequest) {
        CustomApiResponse<TaskDTO.TaskResponse> updatedTask = taskService.updateTask(id, taskRequest);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

    @Operation(summary = "Update the status of a task",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task status updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid status data"),
                    @ApiResponse(responseCode = "404", description = "Task not found")
            })
    @PreAuthorize("@taskSecurity.isTaskOwner(#id)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CustomApiResponse<TaskDTO.TaskResponse>> updateTaskStatus(@PathVariable Long id, @Valid @RequestBody TaskDTO.TaskUpdateStatusRequest statusRequest) {
        CustomApiResponse<TaskDTO.TaskResponse> updatedTask = taskService.updateTaskStatus(id, statusRequest);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

    @Operation(summary = "Delete a task by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Task not found")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomApiResponse<Void>> deleteTask(@PathVariable Long id) {
        CustomApiResponse<Void> response = taskService.deleteTask(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @Operation(summary = "Get all tasks that are overdue",
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved list of overdue tasks"))
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/overdue")
    public ResponseEntity<CustomApiResponse<List<TaskDTO.TaskSummaryResponse>>> getOverdueTasks() {
        CustomApiResponse<List<TaskDTO.TaskSummaryResponse>> overdueTasks = taskService.getOverdueTasks();
        return ResponseEntity.status(HttpStatus.OK).body(overdueTasks);
    }

    @Operation(summary = "Get task counts grouped by status",
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved task counts by status"))
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/counts-by-status")
    public ResponseEntity<CustomApiResponse<List<Object[]>>> getTaskCountsByStatus() {
        CustomApiResponse<List<Object[]>> counts = taskService.getTaskCountsByStatus();
        return ResponseEntity.status(HttpStatus.OK).body(counts);
    }
}
