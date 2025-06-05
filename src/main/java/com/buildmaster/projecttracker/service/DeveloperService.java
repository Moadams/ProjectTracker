package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.ApiResponse;
import com.buildmaster.projecttracker.dto.DeveloperDTO;
import com.buildmaster.projecttracker.mapper.DeveloperMapper;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final DeveloperMapper developerMapper;

    public ApiResponse<Page<DeveloperDTO.DeveloperResponse>> getAllDevelopers(Pageable pageable) {
        Page<DeveloperDTO.DeveloperResponse> response = developerRepository.findAll(pageable).map(developerMapper::toDeveloperResponse);
        return ApiResponse.success("Developers List", response);
    }

    public ApiResponse<DeveloperDTO.DeveloperResponse> createDeveloper(DeveloperDTO.DeveloperRequest request) {
        Developer developer = developerMapper.toDeveloperEntity(request);
        Developer savedDeveloper = developerRepository.save(developer);
        DeveloperDTO.DeveloperResponse response = developerMapper.toDeveloperResponse(savedDeveloper);
        return ApiResponse.success("Developer created", response);
    }
}
