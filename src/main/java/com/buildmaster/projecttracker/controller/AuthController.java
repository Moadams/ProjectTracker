package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.AuthDTO;
import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.model.User;
import com.buildmaster.projecttracker.security.JwtService;
import com.buildmaster.projecttracker.repository.UserRepository;
import com.buildmaster.projecttracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        return ResponseEntity.ok(new AuthDTO.JwtResponse(accessToken, "Bearer", refreshToken,jwtService.getJwtExpiration() / 1000, role));
    }

    @Operation(summary = "Get current logged-in user details", security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/users/me")
    public ResponseEntity<AuthDTO.UserProfileResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logged-in user not found in database."));

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("N/A");

        return ResponseEntity.ok(new AuthDTO.UserProfileResponse(currentUser.getId(), currentUser.getEmail(), role));
    }
}