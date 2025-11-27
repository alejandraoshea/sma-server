package com.example.telemedicine.integration;

import com.example.telemedicine.controller.AuthController;
import com.example.telemedicine.domain.Role;
import com.example.telemedicine.domain.User;
import com.example.telemedicine.repository.DoctorRepository;
import com.example.telemedicine.repository.PatientRepository;
import com.example.telemedicine.security.JwtService;
import com.example.telemedicine.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito .*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito .*;

public class AuthEndpointsTest {
        @InjectMocks
        private AuthController authController;

        @Mock
        private AuthService authService;

        @Mock
        private JwtService jwtService;

        @Mock
        private PatientRepository patientRepository;

        @Mock
        private DoctorRepository doctorRepository;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
        }

        // --- REGISTER tests ---

        @Test
        void register_success() {
            User user = new User();
            // No exception thrown by authService.register()

            ResponseEntity<?> response = authController.register(user);

            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("message", "User registered successfully");

            verify(authService).register(user);
        }

        @Test
        void register_failure() {
            User user = new User();
            doThrow(new RuntimeException("Registration failed")).when(authService).register(user);

            ResponseEntity<?> response = authController.register(user);

            assertThat(response.getStatusCodeValue()).isEqualTo(400);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("error", "Registration failed");
            verify(authService).register(user);
        }

        // --- LOGIN tests ---

        @Test
        void login_patient_success() {
            User loginRequest = new User();
            loginRequest.setEmail("patient@example.com");
            loginRequest.setPassword("password");

            User loggedInUser = new User();
            loggedInUser.setId(1L);
            loggedInUser.setRole(Role.PATIENT);

            when(authService.login("patient@example.com", "password")).thenReturn(loggedInUser);
            when(patientRepository.findPatientIdByUserId(1L)).thenReturn(100L);
            when(jwtService.generateToken(loggedInUser)).thenReturn("token123");

            ResponseEntity<?> response = authController.login(loginRequest);

            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            Map<String, Object> body = (Map<String, Object>) response.getBody();

            assertThat(body).containsEntry("message", "User logged in successfully");
            assertThat(body).containsEntry("role", Role.PATIENT);
            assertThat(body).containsEntry("token", "token123");
            assertThat(body).containsEntry("patientId", 100L);

            verify(authService).login("patient@example.com", "password");
            verify(patientRepository).findPatientIdByUserId(1L);
            verify(jwtService).generateToken(loggedInUser);
        }

        @Test
        void login_doctor_success() {
            User loginRequest = new User();
            loginRequest.setEmail("doctor@example.com");
            loginRequest.setPassword("password");

            User loggedInUser = new User();
            loggedInUser.setId(2L);
            loggedInUser.setRole(Role.DOCTOR);

            when(authService.login("doctor@example.com", "password")).thenReturn(loggedInUser);
            when(doctorRepository.findDoctorIdByUserId(2L)).thenReturn(200L);
            when(jwtService.generateToken(loggedInUser)).thenReturn("token456");

            ResponseEntity<?> response = authController.login(loginRequest);

            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            Map<String, Object> body = (Map<String, Object>) response.getBody();


            assertThat(body).containsEntry("message", "User logged in successfully");
            assertThat(body).containsEntry("role", Role.DOCTOR);
            assertThat(body).containsEntry("token", "token456");
            assertThat(body).containsEntry("doctorId", 200L);

            verify(authService).login("doctor@example.com", "password");
            verify(doctorRepository).findDoctorIdByUserId(2L);
            verify(jwtService).generateToken(loggedInUser);
        }

        @Test
        void login_failure() {
            User loginRequest = new User();
            loginRequest.setEmail("user@example.com");
            loginRequest.setPassword("wrongpassword");

            when(authService.login("user@example.com", "wrongpassword")).thenThrow(new RuntimeException("Invalid credentials"));

            ResponseEntity<?> response = authController.login(loginRequest);

            assertThat(response.getStatusCodeValue()).isEqualTo(400);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("error", "Invalid credentials");

            verify(authService).login("user@example.com", "wrongpassword");
        }
}
