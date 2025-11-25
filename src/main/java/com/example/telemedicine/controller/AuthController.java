package com.example.telemedicine.controller;

import com.example.telemedicine.domain.User;
import com.example.telemedicine.security.JwtService;
import com.example.telemedicine.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


/**
 * REST controller that handles user authentication and registration endpoints.
 */
@RestController
@RequestMapping("/api/authentication")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * Constructs an AuthController with the specified authentication and JWT services.
     *
     * @param authService the service responsible for user registration and login
     * @param jwtService  the service responsible for generating JWT tokens
     */
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }


    /**
     * Registers a new user in the system.
     *
     * @param user the {@link User} object containing registration details
     * @return a ResponseEntity containing success or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            authService.register(user);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    /**
     * Logs in a user using their email and password.
     *
     * @param user the {@link User} object containing email and password
     * @return a ResponseEntity containing JWT token, role, and message, or an error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            User loggedInUser = authService.login(user.getEmail(), user.getPassword());
            String token = jwtService.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User logged in successfully");
            response.put("role", loggedInUser.getRole());
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }
}
