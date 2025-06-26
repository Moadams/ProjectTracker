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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

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

    private volatile Role cachedDeveloperRole;

    public AuthDTO.JwtResponse loginUser(AuthDTO.LoginUserRequest loginRequest){
        String userEmail = loginRequest.email();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        CompletableFuture<String> accessTokenFuture = CompletableFuture.supplyAsync(() ->
                jwtService.generateToken(userDetails));
        CompletableFuture<String> refreshTokenFuture = CompletableFuture.supplyAsync(() ->
                jwtService.generateRefreshToken(userDetails));

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        try{
            String accessToken = accessTokenFuture.get();
            String refreshToken = refreshTokenFuture.get();

            AuthDTO.JwtResponse response = new AuthDTO.JwtResponse(
                    accessToken,"Bearer", refreshToken, jwtService.getJwtExpiration() / 1000, role
            );

            logAuditAsync(ActionType.LOGIN_SUCCESS, EntityType.USER, userEmail,
                    "Successful login for user: " + userEmail, userEmail);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error generating tokens",e);
        }



    }

    @Transactional(timeout = 10)
    public CustomApiResponse<?> registerUser(AuthDTO.RegisterUserRequest requestData) {
        if (userRepository.existsByEmail(requestData.email())) {
            return CustomApiResponse.error("Email already exists");
        }

        Role developerRole =getCachedDeveloperRole();
        String encodedPassword = passwordEncoder.encode(requestData.password());
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .email(requestData.email())
                .password(encodedPassword)
                .roles(Collections.singleton(developerRole))
                .createdAt(now)
                .updatedAt(now)
                .build();
        userRepository.save(user);

        final String userEmail = user.getEmail();
        final String userId = user.getId().toString();

        // Return success response immediately, then handle async operations
        CustomApiResponse<?> response = CustomApiResponse.success("User created successfully", null);

        // Execute async operations AFTER transaction completes
        CompletableFuture.runAsync(() -> {
            createDeveloperProfileAsync(userEmail);
            logAuditAsync(ActionType.CREATE, EntityType.USER, userId,
                    "New user '" + userEmail + "' registered with ROLE_DEVELOPER.", userEmail);
        });

        return response;
    }

    @Cacheable("roles")
    public Role getCachedDeveloperRole(){
        if(cachedDeveloperRole == null){
            synchronized (this){
                if(cachedDeveloperRole == null){
                    cachedDeveloperRole = roleRepository.findByName(RoleName.ROLE_DEVELOPER).orElseGet(()->roleRepository.save(
                            Role.builder().name(RoleName.ROLE_DEVELOPER).build()
                    ));
                }
            }
        }
        return cachedDeveloperRole;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30)
    protected CompletableFuture<Void> createDeveloperProfileAsync(String email){
        return CompletableFuture.runAsync(() -> {
            try{
                if(developerRepository.findByEmail(email).isEmpty()){
                    Developer developer = Developer.builder()
                            .name(email.split("@")[0])
                            .email(email)
                            .build();
                    developerRepository.save(developer);
                }
            }catch (Exception e){
                System.out.println("Failed to create developer profile for: " + email + " - " + e.getMessage());
            }
        });
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 15)
    protected CompletableFuture<Void> logAuditAsync(ActionType actionType, EntityType entityType,
                                                  String entityId, String description, String performedBy) {
        return CompletableFuture.runAsync(() -> {
            try {
                auditLogService.logAudit(actionType, entityType, entityId, description, performedBy);
            } catch (Exception e) {
                // Log error but don't fail the main operation
                System.err.println("Failed to log audit: " + e.getMessage());
            }
        });
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
