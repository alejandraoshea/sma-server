package com.example.telemedicine.controller;

import com.example.telemedicine.domain.User;
import org.springframework.http.ResponseEntity;
import com.example.telemedicine.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

//will be handling authentication: login, register and logout + interacts with AuthService and AuthRepo
@RestController
@RequestMapping("/api/authentication")
@CrossOrigin(origins = "*") //for frontend
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        authService.register(user);
        return ResponseEntity.ok("User registered successfully");
    }

    /*@PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        boolean success = authService.login(email, password);
        if (success) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }*/

}
