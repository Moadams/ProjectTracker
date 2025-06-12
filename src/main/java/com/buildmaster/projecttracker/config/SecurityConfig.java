package com.buildmaster.projecttracker.config;

import com.buildmaster.projecttracker.security.JwtAuthenticationFilter;
import com.buildmaster.projecttracker.service.CustomOAuth2UserService;
import com.buildmaster.projecttracker.util.CustomAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Value("${spring.h2.console.enabled}")
    private boolean h2ConsoleEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless APIs
                .csrf(AbstractHttpConfigurer::disable)
                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Set session management to stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configure request authorization
                .authorizeHttpRequests(authorize -> {
                    // Public endpoints
                    authorize.requestMatchers(
                            "/auth/**",
                            "/error"
                    ).permitAll();

                    // Swagger UI (public during development, restricted later)
                    authorize.requestMatchers(
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/swagger-resources/**",
                            "/webjars/**"
                    ).permitAll();

                    // H2 Console (restricted to ADMIN later, for development)
                    if (h2ConsoleEnabled) {
                        // Use requestMatchers directly with the pattern
                        authorize.requestMatchers("/h2-console/**").permitAll();
                        // Important: Need to disable frame options for H2 console to work in browser
                        try {
                            http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Admin-only endpoints
                    authorize.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");

                    // All other API requests require authentication
                    authorize.requestMatchers("/api/v1/**").authenticated();
                })
                // Configure authentication provider
                .authenticationProvider(authenticationProvider)
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Configure OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth2 login failed: " + exception.getMessage());
                        })
                );

        return http.build();
    }

    // CORS Configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}