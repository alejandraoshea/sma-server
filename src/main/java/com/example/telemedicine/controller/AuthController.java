package com.example.telemedicine.controller;

import com.example.telemedicine.domain.User;
import org.springframework.http.ResponseEntity;
import com.example.telemedicine.service.AuthService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;


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
            System.out.println("Received role from frontend: " + user.getRole());
            authService.register(user);
            return ResponseEntity.ok().body("{\"message\": \"User registered successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
