package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.AuthDTO;
import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user (default role: DEVELOPER)")
    @PostMapping("/register")
    public ResponseEntity<CustomApiResponse<?>> registerUser(@Valid @RequestBody AuthDTO.RegisterUserRequest registerRequest) {
        CustomApiResponse<?> response = authService.registerUser(registerRequest);
        if (!response.success()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login user and receive JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthDTO.JwtResponse> authenticateUser(@Valid @RequestBody AuthDTO.LoginUserRequest loginRequest) {
        AuthDTO.JwtResponse jwtResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @Operation(summary = "Get current logged-in user details", security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/users/me")
    public ResponseEntity<AuthDTO.UserProfileResponse> getCurrentUser() {
        AuthDTO.UserProfileResponse response = authService.getUserProfile();
        return ResponseEntity.ok(response);
    }
}