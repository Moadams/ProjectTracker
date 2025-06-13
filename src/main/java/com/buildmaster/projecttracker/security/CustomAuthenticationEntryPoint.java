package com.buildmaster.projecttracker.security;

import com.buildmaster.projecttracker.dto.CustomApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper; // To convert ErrorDetails to JSON

    /**
     * Commences an authentication scheme. This method is called when an unauthenticated
     * user tries to access a protected resource and no other AuthenticationException handler
     * has been invoked.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.error("Authentication error: {}. Path: {}", authException.getMessage(), request.getRequestURI());


        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);


        CustomApiResponse errorDetails = new CustomApiResponse(
                false,
                "Authentication Required: Access token is missing or invalid.",
                request.getRequestURI(),
                LocalDateTime.now()
        );


        objectMapper.writeValue(response.getOutputStream(), errorDetails);
    }
}
