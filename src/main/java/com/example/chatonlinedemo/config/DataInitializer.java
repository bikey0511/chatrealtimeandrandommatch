package com.example.chatonlinedemo.config;

import com.example.chatonlinedemo.entity.User;
import com.example.chatonlinedemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin if not exists
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .status(User.Status.ACTIVE)
                    .build();
            userRepository.save(admin);
            log.info("✅ Default admin account created: admin / admin123");
        }
        
        // Create sample users for testing
        if (userRepository.count() < 5) {
            createSampleUser("user1", "password123");
            createSampleUser("user2", "password123");
            createSampleUser("user3", "password123");
            log.info("✅ Sample users created for testing");
        }
    }
    
    private void createSampleUser(String username, String password) {
        if (!userRepository.findByUsername(username).isPresent()) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .role(User.Role.USER)
                    .status(User.Status.ACTIVE)
                    .build();
            userRepository.save(user);
        }
    }
}
