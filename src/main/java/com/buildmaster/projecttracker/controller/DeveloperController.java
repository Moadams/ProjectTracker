package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.DeveloperDTO;
import com.buildmaster.projecttracker.service.DeveloperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/developers")
public class DeveloperController {

    private final DeveloperService developerService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeveloperDTO.DeveloperResponse>>> getDevelopers(Pageable pageable) {
        ApiResponse<Page<DeveloperDTO.DeveloperResponse>> developers = developerService.getAllDevelopers(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(developers);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeveloperDTO.DeveloperResponse>> createDeveloper(@Valid @RequestBody DeveloperDTO.DeveloperRequest developerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(developerService.createDeveloper(developerRequest));
    }
}
