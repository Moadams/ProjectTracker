package com.buildmaster.projecttracker.exception;

import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.service.AuditLogService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditLogService auditLogService;

    private String getCurrentUserEmailForAudit() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return "anonymous/unauthenticated";
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {

        return new ResponseEntity<>(CustomApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        CustomApiResponse<Map<String, String>> response = CustomApiResponse.error("Validation failed", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomApiResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        CustomApiResponse errorDetails = new CustomApiResponse(false, "Access Denied: You do not have permission to access this resource.", request.getDescription(false),LocalDateTime.now());
        String userEmail = getCurrentUserEmailForAudit();
        auditLogService.logAudit(ActionType.UNAUTHORIZED_ACCESS, EntityType.USER, null,
                "Access denied for user: " + userEmail + " to " + request.getDescription(false) + ". Reason: " + ex.getMessage(),
                userEmail);

        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomApiResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        CustomApiResponse errorDetails = new CustomApiResponse(false, "Authentication Failed: " + ex.getMessage(), request.getDescription(false), LocalDateTime.now());
        String userEmail = getCurrentUserEmailForAudit();
        auditLogService.logAudit(ActionType.LOGIN_FAILURE, EntityType.USER, null,
                "Authentication failed for user " + userEmail + " to " + request.getDescription(false) + ". Reason: " + ex.getMessage(),
                userEmail);
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<CustomApiResponse> handleUserNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        CustomApiResponse errorDetails = new CustomApiResponse(false, "User not found: " + ex.getMessage(), request.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CustomApiResponse> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        CustomApiResponse errorDetails = new CustomApiResponse(false, "Token has expired: " + ex.getMessage(), request.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<CustomApiResponse> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        CustomApiResponse errorDetails = new CustomApiResponse(false, ex.getMessage(), request.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomApiResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        CustomApiResponse errorDetails = new CustomApiResponse(false, ex.getMessage(), request.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomApiResponse> handleGlobalException(Exception ex, WebRequest request) {

        return new ResponseEntity<>(CustomApiResponse.error(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
