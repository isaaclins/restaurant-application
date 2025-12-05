package com.restaurant.auth.config;

import com.restaurant.auth.entity.Role;
import com.restaurant.auth.entity.User;
import com.restaurant.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Loads initial data for development and testing.
 * Creates default admin and staff accounts if they don't exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test") // Don't run during tests
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createAdminUser();
        createStaffUser();
    }

    private void createAdminUser() {
        String adminEmail = "admin@restaurant.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.RESTAURANT_ADMIN)
                    .active(true)
                    .build();

            userRepository.save(admin);
            log.info("Created default admin user: {}", adminEmail);
        } else {
            log.info("Admin user already exists: {}", adminEmail);
        }
    }

    private void createStaffUser() {
        String staffEmail = "staff@restaurant.com";

        if (!userRepository.existsByEmail(staffEmail)) {
            User staff = User.builder()
                    .email(staffEmail)
                    .password(passwordEncoder.encode("staff123"))
                    .firstName("Staff")
                    .lastName("Member")
                    .role(Role.RESTAURANT_STAFF)
                    .active(true)
                    .build();

            userRepository.save(staff);
            log.info("Created default staff user: {}", staffEmail);
        } else {
            log.info("Staff user already exists: {}", staffEmail);
        }
    }
}
