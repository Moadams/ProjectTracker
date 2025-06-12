package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.enums.RoleName;
import com.buildmaster.projecttracker.model.Role;
import com.buildmaster.projecttracker.model.User;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.repository.RoleRepository;
import com.buildmaster.projecttracker.repository.UserRepository;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import com.buildmaster.projecttracker.util.CustomOAuth2User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private DeveloperRepository developerRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OidcUserRequest oidcUserRequest;
    private DefaultOidcUser defaultOidcUser;
    private final String TEST_EMAIL = "test_oauth@google.com";

    @BeforeEach
    void setUp() {

        Map<String, Object> idTokenClaims = new HashMap<>();
        idTokenClaims.put("sub", "1234567890");
        idTokenClaims.put("email", TEST_EMAIL);
        idTokenClaims.put("name", "Test User");
        OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(), Instant.now().plusSeconds(3600), idTokenClaims);


        Map<String, Object> userInfoClaims = new HashMap<>();
        userInfoClaims.put("email", TEST_EMAIL);
        OidcUserInfo userInfo = new OidcUserInfo(userInfoClaims);

        // Mock DefaultOidcUser
        Set<OidcUserAuthority> authorities = Collections.singleton(new OidcUserAuthority("ROLE_USER", idToken, userInfo));
        defaultOidcUser = new DefaultOidcUser(authorities, idToken, userInfo);

        // Mock OidcUserRequest
        oidcUserRequest = mock(OidcUserRequest.class);
        when(oidcUserRequest.getIdToken()).thenReturn(idToken);



        try {
            doReturn(defaultOidcUser).when(customOAuth2UserService).loadUser(any(OidcUserRequest.class));
        } catch (Exception e) {
            // This should not happen in a controlled mock environment
            fail("Failed to mock super.loadUser: " + e.getMessage());
        }

    }

    @Test
    @DisplayName("Should create new user and developer profile for first-time OAuth2 login")
    void loadUser_shouldCreateNewUserAndDeveloper() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty()); // User does not exist
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword"); // Mock password encoder
        when(roleRepository.findByName(RoleName.ROLE_CONTRACTOR)).thenReturn(Optional.of(Role.builder().name(RoleName.ROLE_CONTRACTOR).build())); // Role exists
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved user
        when(developerRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty()); // Developer does not exist
        when(developerRepository.save(any(Developer.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved developer


        CustomOAuth2User customOAuth2User = (CustomOAuth2User) customOAuth2UserService.loadUser(oidcUserRequest);

        assertThat(customOAuth2User).isNotNull();
        assertThat(customOAuth2User.getUsername()).isEqualTo(TEST_EMAIL);
        assertThat(customOAuth2User.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_CONTRACTOR"));

        verify(userRepository, times(2)).save(any(User.class)); // One for initial save, one for linking developerProfile
        verify(userRepository, times(2)).flush(); // One for initial save, one for linking developerProfile
        verify(developerRepository, times(1)).save(any(Developer.class));
        verify(developerRepository, times(1)).flush();
        verify(roleRepository, times(1)).findByName(RoleName.ROLE_CONTRACTOR);
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    @DisplayName("Should update existing user and ensure developer profile exists for OAuth2 login")
    void loadUser_shouldUpdateExistingUserAndEnsureDeveloper() {
        Set<Role> existingRoles = new HashSet<>();
        existingRoles.add(Role.builder().name(RoleName.ROLE_CONTRACTOR).build());
        User existingUser = User.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .password("oldHashedPassword")
                .roles(existingRoles)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(5))
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser)); // User exists
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved user


        CustomOAuth2User customOAuth2User = (CustomOAuth2User) customOAuth2UserService.loadUser(oidcUserRequest);

        assertThat(customOAuth2User).isNotNull();
        assertThat(customOAuth2User.getUsername()).isEqualTo(TEST_EMAIL);
        assertThat(customOAuth2User.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_CONTRACTOR"));

        verify(userRepository, times(1)).save(existingUser); // Should update existing user once
        verify(userRepository, times(1)).flush();
        verify(developerRepository, never()).save(any(Developer.class)); // Should not create new developer
        verify(developerRepository, never()).flush();
        verify(roleRepository, never()).findByName(any()); // No role creation needed
        verify(passwordEncoder, never()).encode(anyString()); // No password encoding needed
        assertThat(existingUser.getUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1)); // Updated timestamp check
    }

    @Test
    @DisplayName("Should throw exception if email is not provided by OAuth2 provider")
    void loadUser_shouldThrowExceptionIfEmailMissing() {
        // Mock OidcUserInfo without email
        Map<String, Object> userInfoClaims = new HashMap<>();
        OidcUserInfo userInfoWithoutEmail = new OidcUserInfo(userInfoClaims);

        OidcIdToken idTokenWithoutEmail = new OidcIdToken("tokenValue", Instant.now(), Instant.now().plusSeconds(3600), new HashMap<>());

        DefaultOidcUser oidcUserWithoutEmail = new DefaultOidcUser(Collections.singleton(new OidcUserAuthority("ROLE_USER", idTokenWithoutEmail, userInfoWithoutEmail)), idTokenWithoutEmail, userInfoWithoutEmail);

        // Mock the superclass loadUser to return this user without email
        try {
            doReturn(oidcUserWithoutEmail).when(customOAuth2UserService).loadUser(any(OidcUserRequest.class));
        } catch (Exception e) {
            fail("Failed to mock super.loadUser for missing email test: " + e.getMessage());
        }

        // Mock OidcUserRequest to return the user without email
        OidcUserRequest requestWithoutEmail = mock(OidcUserRequest.class);
        when(requestWithoutEmail.getIdToken()).thenReturn(idTokenWithoutEmail);



        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class,
                () -> customOAuth2UserService.loadUser(requestWithoutEmail));

        assertThat(exception.getMessage()).contains("OAuth2 provider did not provide an email address.");
        verify(userRepository, never()).findByEmail(anyString());
    }
}
