package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.dto.DeveloperDTO;
import com.buildmaster.projecttracker.service.DeveloperService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Developer Management", description = "Operations related to developers")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/developers")
public class DeveloperController {

    private final DeveloperService developerService;

    @Operation(summary = "Get all developers with pagination and sorting",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Number of records per page", example = "10"),
                    @Parameter(name = "sort", description = "Sort order (field,asc/desc)", example = "name,asc")
            },
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved list of developers"))
    @GetMapping
    public ResponseEntity<CustomApiResponse<Page<DeveloperDTO.DeveloperSummaryResponse>>> getDevelopers(Pageable pageable) {
        CustomApiResponse<Page<DeveloperDTO.DeveloperSummaryResponse>> developers = developerService.getAllDevelopers(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(developers);
    }

    @Operation(summary = "Create a new developer",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Developer created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid developer data")
            })
    @PostMapping
    public ResponseEntity<CustomApiResponse<DeveloperDTO.DeveloperResponse>> createDeveloper(@Valid @RequestBody DeveloperDTO.DeveloperRequest developerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(developerService.createDeveloper(developerRequest));
    }


    @Operation(summary = "Get a developer by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Developer found"),
                    @ApiResponse(responseCode = "404", description = "Developer not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<CustomApiResponse<DeveloperDTO.DeveloperResponse>> getDeveloper(@PathVariable Long id) {
        CustomApiResponse<DeveloperDTO.DeveloperResponse> developer = developerService.getDeleveloperById(id);
        return ResponseEntity.status(HttpStatus.OK).body(developer);
    }

    @Operation(summary = "Update an existing developer",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Developer updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid developer data"),
                    @ApiResponse(responseCode = "404", description = "Developer not found")
            })
    @PutMapping("/{id}")
    public ResponseEntity<CustomApiResponse<DeveloperDTO.DeveloperResponse>> updateDeveloper(@PathVariable Long id, @Valid @RequestBody DeveloperDTO.DeveloperRequest developerRequest) {
        CustomApiResponse<DeveloperDTO.DeveloperResponse> developer = developerService.updateDeveloper(id, developerRequest);
        return ResponseEntity.status(HttpStatus.OK).body(developer);
    }

    @Operation(summary = "Delete a developer by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Developer deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Developer not found")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomApiResponse<Void>> deleteDeveloper(@PathVariable Long id) {
        CustomApiResponse<Void> response = developerService.deleteDeveloper(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @Operation(summary = "Get top 5 developers with the most tasks assigned",
            responses = @ApiResponse(responseCode = "200", description = "Successfully retrieved list of top developers"))
    @GetMapping("/top-5-most-tasks")
    public ResponseEntity<CustomApiResponse<List<DeveloperDTO.DeveloperSummaryResponse>>> getTop5DevelopersWithMostTasks() {
        CustomApiResponse<List<DeveloperDTO.DeveloperSummaryResponse>> developers = developerService.getTop5DevelopersWithMostTasks();
        return ResponseEntity.status(HttpStatus.OK).body(developers);
    }
}
