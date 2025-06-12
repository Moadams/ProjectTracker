package com.buildmaster.projecttracker.util;

import com.buildmaster.projecttracker.dto.AuthDTO;
import com.buildmaster.projecttracker.model.User;
import com.buildmaster.projecttracker.repository.UserRepository;
import com.buildmaster.projecttracker.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Still needed for local login context, but not for re-loading OAuth2 user
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional; // Keep Optional import

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetails authenticatedUserDetails = null;
        String userEmail = null;

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            authenticatedUserDetails = (UserDetails) principal;
            userEmail = authenticatedUserDetails.getUsername();
        } else if (principal instanceof OidcUser) {

            OidcUser oidcUser = (OidcUser) principal;
            userEmail = oidcUser.getEmail();
            Optional<User> localUserOptional = userRepository.findByEmail(userEmail);
            if (localUserOptional.isPresent()) {
                authenticatedUserDetails = localUserOptional.get();
            }
        } else if (principal instanceof OAuth2User) {

            OAuth2User oauth2User = (OAuth2User) principal;
            userEmail = oauth2User.getAttribute("email");
            if (userEmail == null) {
                userEmail = oauth2User.getName();
            }
            Optional<User> localUserOptional = userRepository.findByEmail(userEmail);
            if (localUserOptional.isPresent()) {
                authenticatedUserDetails = localUserOptional.get();
            }
        }


        if (authenticatedUserDetails == null || userEmail == null || userEmail.isEmpty()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load authenticated user details for JWT generation. Email: " + userEmail + " (principal type: " + (principal != null ? principal.getClass().getName() : "null") + ")");
            httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
            return;
        }


        String jwtToken = jwtService.generateToken(authenticatedUserDetails);
        String refreshToken = jwtService.generateRefreshToken(authenticatedUserDetails);
        String role = authenticatedUserDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        AuthDTO.JwtResponse jwtResponse = new AuthDTO.JwtResponse(
                jwtToken,
                "Bearer",
                refreshToken,
                jwtService.getJwtExpiration() / 1000,
                role
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(jwtResponse));
        response.setStatus(HttpServletResponse.SC_OK);

        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}