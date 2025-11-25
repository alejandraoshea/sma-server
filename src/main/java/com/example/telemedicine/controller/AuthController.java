package com.example.telemedicine.controller;

import com.example.telemedicine.domain.User;
import com.example.telemedicine.domain.Role;
import com.example.telemedicine.repository.DoctorRepository;
import com.example.telemedicine.repository.PatientRepository;
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
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    /**
     * Constructs an AuthController with the specified authentication and JWT services.
     *
     * @param authService the service responsible for user registration and login
     * @param jwtService  the service responsible for generating JWT tokens
     */
    public AuthController(AuthService authService, JwtService jwtService,
                          PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
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

            if (loggedInUser.getRole() == Role.PATIENT) {
                Long patientId = patientRepository.findPatientIdByUserId(loggedInUser.getId());
                loggedInUser.setPatientId(patientId);
            } else if (loggedInUser.getRole() == Role.DOCTOR) {
                Long doctorId = doctorRepository.findDoctorIdByUserId(loggedInUser.getId());
                loggedInUser.setDoctorId(doctorId);
            }

            String token = jwtService.generateToken(loggedInUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User logged in successfully");
            response.put("role", loggedInUser.getRole());
            response.put("token", token);

            if (loggedInUser.getPatientId() != null) response.put("patientId", loggedInUser.getPatientId());
            if (loggedInUser.getDoctorId() != null) response.put("doctorId", loggedInUser.getDoctorId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }
}
