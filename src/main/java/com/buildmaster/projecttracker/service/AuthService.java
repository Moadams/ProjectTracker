package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.AuthDTO;
import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.enums.RoleName;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.model.Role;
import com.buildmaster.projecttracker.model.User;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import com.buildmaster.projecttracker.repository.RoleRepository;
import com.buildmaster.projecttracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    public final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public CustomApiResponse<?> registerUser(AuthDTO.RegisterUserRequest requestData) {
        if (userRepository.existsByEmail(requestData.email())) {
            return CustomApiResponse.error("Email already exists");
        }

        Role developerRole = roleRepository.findByName(RoleName.ROLE_DEVELOPER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_DEVELOPER).build()));

        User user = User.builder()
                .email(requestData.email())
                .password(passwordEncoder.encode(requestData.password()))
                .roles(Collections.singleton(developerRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        if (user.getRoles().contains(developerRole)) {
            if (developerRepository.findByEmail(user.getEmail()).isEmpty()) {
                Developer developer = Developer.builder()
                        .name(user.getEmail().split("@")[0])
                        .email(user.getEmail())
                        .build();
                developerRepository.save(developer);
            }
        }

        return CustomApiResponse.success("User created successfully", null);
    }
}
