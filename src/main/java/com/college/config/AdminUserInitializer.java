
package com.college.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserInitializer {

    @Bean
    public ApplicationRunner createAdmin(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String username = System.getenv("ADMIN_USERNAME");
            String password = System.getenv("ADMIN_PASSWORD");

            if (username == null || password == null ||
                username.isBlank() || password.isBlank()) {

                System.out.println(
                    "Admin creation skipped: environment variables not set."
                );
                return;
            }

            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class,
                username
            );

            if (count != null && count > 0) {
                System.out.println("Admin account already exists.");
                return;
            }

            String hashedPassword =
                    passwordEncoder.encode(password);

            jdbcTemplate.update(
                "INSERT INTO users (username, password, role) " +
                "VALUES (?, ?, 'ADMIN')",
                username,
                hashedPassword
            );

            System.out.println("Admin account created successfully.");
        };
    }
}
