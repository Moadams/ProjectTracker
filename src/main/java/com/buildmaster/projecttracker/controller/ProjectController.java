package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectDTO.ProjectResponse> createProject(@RequestBody ProjectDTO.ProjectRequest projectRequest) {
        ProjectDTO.ProjectResponse createdProject = projectService.createProject(projectRequest);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }
}
