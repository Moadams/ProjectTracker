package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@Validated
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectDTO.ProjectResponse>>> getAllProjects(Pageable pageable) {
        ApiResponse<Page<ProjectDTO.ProjectResponse>> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(projects);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDTO.ProjectResponse>> createProject(@RequestBody ProjectDTO.ProjectRequest projectRequest) {
        ApiResponse<ProjectDTO.ProjectResponse> createdProject = projectService.createProject(projectRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO.ProjectResponse>> getProjectById(@PathVariable Long id) {
        ApiResponse<ProjectDTO.ProjectResponse> project = projectService.getProjectById(id);
        return ResponseEntity.status(HttpStatus.OK).body(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO.ProjectResponse>> updateProject(@PathVariable Long id, @RequestBody ProjectDTO.ProjectUpdateRequest projectRequest) {
        ApiResponse<ProjectDTO.ProjectResponse> project = projectService.updateProject(id, projectRequest);
        return ResponseEntity.status(HttpStatus.OK).body(project);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        ApiResponse<Void> project = projectService.deleteProject(id);
        return ResponseEntity.status(HttpStatus.OK).body(project);
    }

    @GetMapping("/without-tasks")
    public ResponseEntity<ApiResponse<List<ProjectDTO.ProjectSummaryResponse>>> getProjectsWithoutTasks() {
        ApiResponse<List<ProjectDTO.ProjectSummaryResponse>> projects = projectService.getProjectsWithoutTasks();
        return ResponseEntity.status(HttpStatus.OK).body(projects);
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<ProjectDTO.ProjectResponse>>> getProjectsWithOverdue() {
        ApiResponse<List<ProjectDTO.ProjectResponse>> overdueProjects = projectService.getOverdueProjects();
        return ResponseEntity.status(HttpStatus.OK).body(overdueProjects);
    }

}
