package com.buildmaster.projecttracker.config;

import com.buildmaster.projecttracker.enums.RoleName;
import com.buildmaster.projecttracker.model.Role;
import com.buildmaster.projecttracker.model.User;
import com.buildmaster.projecttracker.repository.RoleRepository;
import com.buildmaster.projecttracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail = "admin@buildmater.com";

    @Override
    public void run(String... args) throws Exception {

        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                System.out.println("Created role: " + roleName.name());
            }
        });

        if(userRepository.findByEmail(adminEmail).isEmpty()) {
            System.out.println("Creating admin account");

            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseGet( () -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));

            User adminUser = User.builder().email(adminEmail).password(passwordEncoder.encode("adminpassword123")).roles(Collections.singleton(adminRole)).build();
            userRepository.save(adminUser);
            System.out.println("Created admin user: " + adminEmail);
        }
    }
}