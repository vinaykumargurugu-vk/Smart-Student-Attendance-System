
package com.college.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            AppUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            SecurityContextRepository securityContextRepository)
            throws Exception {

        http
            .authenticationProvider(authenticationProvider)

            .securityContext(context -> context
                .securityContextRepository(securityContextRepository)
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Public login resources
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/logout",
                    "/login.html",
                    "/login.css",
                    "/login.js",
                    "/style.css",
                    "/favicon.ico"
                ).permitAll()

                // Authenticated user information
                .requestMatchers("/api/auth/me").authenticated()

                // Admin dashboard pages
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/students.html",
                    "/attendance.html",
                    "/marks.html",
                    "/performance.html",
                    "/reports.html"
                ).hasRole("ADMIN")

                // Faculty dashboard
                .requestMatchers("/faculty.html")
                    .hasRole("FACULTY")

                // Student dashboard
                .requestMatchers("/student.html")
                    .hasRole("STUDENT")

                // Frontend resources
                .requestMatchers(
                    "/faculty.css",
                    "/faculty.js",
                    "/faculty-*.js",
                    "/student.css",
                    "/student.js",
                    "/attendance.js",
                    "/marks.js",
                    "/performance.js",
                    "/performance.css",
                    "/reports.js"
                ).permitAll()

                // Admin-only APIs
                .requestMatchers(
                    "/api/dashboard/**",
                    "/api/reports/**",
                    "/api/performance/**"
                ).hasRole("ADMIN")

                // Student APIs
                .requestMatchers("/api/student/**")
                    .hasRole("STUDENT")

                // Faculty APIs
                .requestMatchers("/api/faculty/**")
                    .hasRole("FACULTY")

                // Student list: Admin and Faculty can view
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/students",
                    "/api/students/**"
                ).hasAnyRole("ADMIN", "FACULTY")

                // Student creation: Admin only
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/students"
                ).hasRole("ADMIN")

                // Student update: Admin only
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/students/**"
                ).hasRole("ADMIN")

                // Student deletion: Admin only
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/students/**"
                ).hasRole("ADMIN")

                // Attendance and marks APIs
                .requestMatchers(
                    "/api/attendance/**",
                    "/api/marks/**"
                ).hasAnyRole("ADMIN", "FACULTY")

                // Deny all other API requests
                .requestMatchers("/api/**").denyAll()

                // Other resources
                .anyRequest().permitAll()
            )

            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
