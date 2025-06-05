package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.mapper.TaskMapper;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.model.Task;
import com.buildmaster.projecttracker.repository.ProjectRepository;
import com.buildmaster.projecttracker.repository.TaskRepository;
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


    public ApiResponse<Page<TaskDTO.TaskResponse>> getAllTasks(Pageable pageable) {
        Page<TaskDTO.TaskResponse> response = taskRepository.findAll(pageable).map(taskMapper::toTaskDTO);
        return ApiResponse.success("Tasks List", response);
    }

    @Transactional
    public ApiResponse<TaskDTO.TaskResponse> createTask(TaskDTO.TaskRequest request) {
        Task task = taskMapper.toTaskEntity(request);
        Task savedTask = taskRepository.save(task);
        TaskDTO.TaskResponse response = taskMapper.toTaskDTO(savedTask);
        return ApiResponse.success("Task created", response);
    }
}
