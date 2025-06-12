package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.enums.RoleName;
import com.buildmaster.projecttracker.model.Role;
import com.buildmaster.projecttracker.model.User;
import com.buildmaster.projecttracker.repository.RoleRepository;
import com.buildmaster.projecttracker.repository.UserRepository;
import com.buildmaster.projecttracker.util.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        if (email == null || email.isEmpty()) {
            logger.error("OAuth2 provider did not provide an email address for user: {}", oauth2User.getName());
            throw new OAuth2AuthenticationException("OAuth2 provider did not provide an email address.");
        }

        logger.info("Processing OAuth2 login for email: {}", email);
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            logger.info("Existing user found for email: {}", email);

            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Updated existing user: {}", user.getEmail());
        } else {

            logger.info("New OAuth2 user detected: {}. Creating local account with ROLE_CONTRACTOR.", email);
            Role contractorRole = roleRepository.findByName(RoleName.ROLE_CONTRACTOR)
                    .orElseGet(() -> {
                        logger.info("ROLE_CONTRACTOR not found. Creating it.");
                        return roleRepository.save(Role.builder().name(RoleName.ROLE_CONTRACTOR).build());
                    });

            user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("oauth2_dummy_password_" + System.currentTimeMillis()))
                    .roles(Collections.singleton(contractorRole))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
            logger.info("Successfully created new local user for OAuth2: {}", user.getEmail());
        }


        return new CustomOAuth2User(oauth2User, user.getAuthorities());
    }
}