package com.BusBooking.Bus.seed;

import com.BusBooking.Bus.model.User;
import com.BusBooking.Bus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@bus.com").isEmpty()) {
            User admin = User.builder()
                    .name("Super Admin")
                    .email("admin@bus.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build();
            userRepository.save(admin);
            System.out.println("✅ Admin seeded → admin@bus.com / admin123");
        } else {
            System.out.println("ℹ️ Admin already exists.");
        }

        if (userRepository.findByEmail("user@bus.com").isEmpty()) {
            User user = User.builder()
                    .name("Test User")
                    .email("user@bus.com")
                    .password(passwordEncoder.encode("user123"))
                    .role("USER")
                    .build();
            userRepository.save(user);
            System.out.println("✅ User seeded → user@bus.com / user123");
        } else {
            System.out.println("ℹ️ User already exists.");
        }
    }
}
