package com.example.telemedicine.controller;

import com.example.telemedicine.domain.User;
import com.example.telemedicine.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

//will be handling authentication: login, register and logout + interacts with AuthService and AuthRepo
@RestController
@RequestMapping("/api/authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService, JdbcTemplate jdbcTemplate) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            authService.register(user);
            return ResponseEntity.ok().body("{\"message\": \"User registered successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            User loggedInUser = authService.login(user.getEmail(), user.getPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User logged in successfully");
            response.put("role", loggedInUser.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
