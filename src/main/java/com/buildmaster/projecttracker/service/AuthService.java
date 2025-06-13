package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.AuthDTO;
import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.enums.RoleName;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.model.Role;
import com.buildmaster.projecttracker.model.User;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import com.buildmaster.projecttracker.repository.RoleRepository;
import com.buildmaster.projecttracker.repository.UserRepository;
import com.buildmaster.projecttracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    public final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;
    private final JwtService jwtService;

    public AuthDTO.JwtResponse loginUser(AuthDTO.LoginUserRequest loginRequest){
        String userEmail = loginRequest.email();

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

        AuthDTO.JwtResponse response = new AuthDTO.JwtResponse(
                accessToken,"Bearer", refreshToken, jwtService.getJwtExpiration() / 1000, role
        );

        auditLogService.logAudit(ActionType.LOGIN_SUCCESS, EntityType.USER, userEmail,
                "Successful login for user: " + userEmail, userEmail);

        return response;
    }

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

        auditLogService.logAudit(ActionType.CREATE, EntityType.USER, user.getId().toString(),
                "New user '" + user.getEmail() + "' registered with ROLE_DEVELOPER.", user.getEmail());

        return CustomApiResponse.success("User created successfully", null);
    }

    public AuthDTO.UserProfileResponse getUserProfile() {
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

        AuthDTO.UserProfileResponse response = new AuthDTO.UserProfileResponse(
                currentUser.getId(),currentUser.getEmail(), role
        );
        return response;
    }
}
