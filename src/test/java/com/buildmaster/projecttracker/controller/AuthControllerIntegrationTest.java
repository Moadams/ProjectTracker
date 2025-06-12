package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.AuthDTO;
import com.buildmaster.projecttracker.enums.RoleName;
import com.buildmaster.projecttracker.model.Role;
import com.buildmaster.projecttracker.model.User;
import com.buildmaster.projecttracker.repository.DeveloperRepository; // Used for setup
import com.buildmaster.projecttracker.repository.RoleRepository;
import com.buildmaster.projecttracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional; // For rolling back tests

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DeveloperRepository developerRepository;

    @BeforeEach
    void setUp() {

        if (roleRepository.findByName(RoleName.ROLE_DEVELOPER).isEmpty()) {
            roleRepository.save(Role.builder().name(RoleName.ROLE_DEVELOPER).build());
        }
        if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build());
        }
    }

    @Test
    @DisplayName("Should register a new user and create developer profile")
    void registerUser_shouldRegisterNewUserAndCreateDeveloper() throws Exception {
        AuthDTO.RegisterUserRequest registerRequest = new AuthDTO.RegisterUserRequest(
                "newuser@example.com", "securepassword");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());


        // Verify user created in DB
        User registeredUser = userRepository.findByEmail("newuser@example.com").orElse(null);
        assertThat(registeredUser).isNotNull();
        assertThat(passwordEncoder.matches("securepassword", registeredUser.getPassword())).isTrue();
        assertThat(registeredUser.getRoles()).anyMatch(role -> role.getName().equals(RoleName.ROLE_DEVELOPER));
        assertThat(developerRepository.findByEmail("newuser@example.com")).isPresent();

    }

    @Test
    @DisplayName("Should return BAD_REQUEST if email already exists during registration")
    void registerUser_shouldReturnBadRequestForExistingEmail() throws Exception {
        // Register a user first
        AuthDTO.RegisterUserRequest registerRequest = new AuthDTO.RegisterUserRequest(
                "existing@example.com", "password");
        userRepository.save(User.builder()
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_DEVELOPER).get()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Try to register again with the same email
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Should login user and return JWT token")
    void authenticateUser_shouldLoginAndReturnJwt() throws Exception {
        // Register a user for login
        AuthDTO.RegisterUserRequest registerRequest = new AuthDTO.RegisterUserRequest(
                "loginuser@example.com", "loginpassword");
        userRepository.save(User.builder()
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_DEVELOPER).get()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        AuthDTO.LoginUserRequest loginRequest = new AuthDTO.LoginUserRequest(
                "loginuser@example.com", "loginpassword");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.role").value("ROLE_DEVELOPER"))
                .andReturn();

    }

    @Test
    @DisplayName("Should return UNAUTHORIZED for invalid login credentials")
    void authenticateUser_shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Attempt to login with non-existent user
        AuthDTO.LoginUserRequest loginRequest = new AuthDTO.LoginUserRequest(
                "nonexistent@example.com", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("Authentication Failed: Bad credentials"));
    }
}
