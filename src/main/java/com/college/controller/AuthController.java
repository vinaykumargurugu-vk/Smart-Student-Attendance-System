
package com.college.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {

        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse httpResponse) {

        Map<String, Object> response = new HashMap<>();

        if (request.username() == null ||
            request.password() == null ||
            request.username().isBlank() ||
            request.password().isBlank()) {

            response.put("success", false);
            response.put("message", "Username and password are required.");

            return ResponseEntity.badRequest().body(response);
        }

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                        )
                    );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(
                    context, httpRequest, httpResponse);

            Map<String, Object> user =
                    new HashMap<>();

            user.put("username", authentication.getName());

            String role = authentication.getAuthorities()
                    .iterator().next().getAuthority()
                    .replace("ROLE_", "");

            user.put("role", role);

            HttpSession session = httpRequest.getSession(false);

            response.put("success", true);
            response.put("message", "Login successful!");
            response.put("username", user.get("username"));
            response.put("role", role);
            response.put("sessionCreated", session != null);

            return ResponseEntity.ok(response);

        } catch (org.springframework.security.core.AuthenticationException ex) {

            SecurityContextHolder.clearContext();

            response.put("success", false);
            response.put("message", "Invalid username or password.");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> currentUser(
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("username", authentication.getName());

        String role = authentication.getAuthorities()
                .iterator().next().getAuthority()
                .replace("ROLE_", "");

        response.put("role", role);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("message", "Logout successful!");

        return ResponseEntity.ok(response);
    }

    public record LoginRequest(String username, String password) {
    }
}
