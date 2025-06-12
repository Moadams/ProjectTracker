package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.AuthDTO;
import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.model.User;
import com.buildmaster.projecttracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public List<AuthDTO.UserProfileResponse> getRegisteredUsers(){
        List<User> users = userRepository.findAll();
        List<AuthDTO.UserProfileResponse> userProfiles = users.stream()
                .map(user -> new AuthDTO.UserProfileResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse("N/A")
                ))
                .collect(Collectors.toList());
        return userProfiles;
    }

    public void deleteUser(Long id){
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
