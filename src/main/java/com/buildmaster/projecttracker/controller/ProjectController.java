package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@Validated
@Slf4j
@Tag(name = "Project Management", description = "Operations related to projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Get all projects with pagination and sorting",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Number of records per page", example = "10"),
                    @Parameter(name = "sort", description = "Sort order (field,asc/desc)", example = "name,asc")
            },
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved list of projects"))
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<CustomApiResponse<Page<ProjectDTO.ProjectSummaryResponse>>> getAllProjects(Pageable pageable) {
        CustomApiResponse<Page<ProjectDTO.ProjectSummaryResponse>> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(projects);
    }

    @Operation(summary = "Create a new project",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Project created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid project data")
            })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<CustomApiResponse<ProjectDTO.ProjectResponse>> createProject(@Valid @RequestBody ProjectDTO.ProjectRequest projectRequest) {
        CustomApiResponse<ProjectDTO.ProjectResponse> createdProject = projectService.createProject(projectRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @Operation(summary = "Get a project by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Project found"),
                    @ApiResponse(responseCode = "404", description = "Project not found")
            })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<CustomApiResponse<ProjectDTO.ProjectResponse>> getProjectById(@PathVariable Long id) {
        CustomApiResponse<ProjectDTO.ProjectResponse> project = projectService.getProjectById(id);
        return ResponseEntity.status(HttpStatus.OK).body(project);
    }


    @Operation(summary = "Update an existing project",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Project updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid project data"),
                    @ApiResponse(responseCode = "404", description = "Project not found")
            })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<CustomApiResponse<ProjectDTO.ProjectResponse>> updateProject(@PathVariable Long id, @RequestBody ProjectDTO.ProjectUpdateRequest projectRequest) {
        CustomApiResponse<ProjectDTO.ProjectResponse> project = projectService.updateProject(id, projectRequest);
        return ResponseEntity.status(HttpStatus.OK).body(project);
    }

    @Operation(summary = "Delete a project by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Project not found")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomApiResponse<Void>> deleteProject(@PathVariable Long id) {
        CustomApiResponse<Void> project = projectService.deleteProject(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(project);
    }


    @Operation(summary = "Get projects without any assigned tasks",
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved list of projects without tasks"))
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/without-tasks")
    public ResponseEntity<CustomApiResponse<List<ProjectDTO.ProjectSummaryResponse>>> getProjectsWithoutTasks() {
        CustomApiResponse<List<ProjectDTO.ProjectSummaryResponse>> projects = projectService.getProjectsWithoutTasks();
        return ResponseEntity.status(HttpStatus.OK).body(projects);
    }

    @Operation(summary = "Get overdue projects",
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved list of overdue projects "))
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/overdue")
    public ResponseEntity<CustomApiResponse<List<ProjectDTO.ProjectResponse>>> getProjectsWithOverdue() {
        CustomApiResponse<List<ProjectDTO.ProjectResponse>> overdueProjects = projectService.getOverdueProjects();
        return ResponseEntity.status(HttpStatus.OK).body(overdueProjects);
    }

}
