package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskDTO.TaskResponse>>> getTasks(Pageable pageable) {
        ApiResponse<Page<TaskDTO.TaskResponse>> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskDTO.TaskResponse>> createTask(@Valid @RequestBody TaskDTO.TaskRequest request){
        ApiResponse<TaskDTO.TaskResponse> createdTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO.TaskResponse>> getTaskById(@PathVariable Long id) {
        ApiResponse<TaskDTO.TaskResponse> task = taskService.getTaskById(id);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskDTO.TaskSummaryResponse>>> getTasksByProjectId(@PathVariable Long projectId) {
        ApiResponse<List<TaskDTO.TaskSummaryResponse>> tasks = taskService.getTasksByProjectId(projectId);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @GetMapping("/developer/{developerId}")
    public ResponseEntity<ApiResponse<List<TaskDTO.TaskSummaryResponse>>> getTasksByDeveloperId(@PathVariable Long developerId) {
        ApiResponse<List<TaskDTO.TaskSummaryResponse>> tasks = taskService.getTasksByDeveloperId(developerId);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO.TaskResponse>> updateTask(@Valid @PathVariable Long id, @Valid @RequestBody TaskDTO.TaskUpdateRequest taskRequest) {
        ApiResponse<TaskDTO.TaskResponse> updatedTask = taskService.updateTask(id, taskRequest);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskDTO.TaskResponse>> updateTaskStatus(@PathVariable Long id, @Valid @RequestBody TaskDTO.TaskUpdateStatusRequest statusRequest) {
        ApiResponse<TaskDTO.TaskResponse> updatedTask = taskService.updateTaskStatus(id, statusRequest);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        ApiResponse<Void> response = taskService.deleteTask(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskDTO.TaskSummaryResponse>>> getOverdueTasks() {
        ApiResponse<List<TaskDTO.TaskSummaryResponse>> overdueTasks = taskService.getOverdueTasks();
        return ResponseEntity.status(HttpStatus.OK).body(overdueTasks);
    }

    @GetMapping("/counts-by-status")
    public ResponseEntity<ApiResponse<List<Object[]>>> getTaskCountsByStatus() {
        ApiResponse<List<Object[]>> counts = taskService.getTaskCountsByStatus();
        return ResponseEntity.status(HttpStatus.OK).body(counts);
    }
}
