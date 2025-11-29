package com.example.telemedicine.integration;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.security.JwtService;
import com.example.telemedicine.service.AuthService;
import com.example.telemedicine.service.DoctorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DoctorEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private DoctorService doctorService;

    @MockBean
    private JwtService jwtService;

    private Long doctorId;
    private String email = "testdoctor@example.com";
    private String password = "password123";

    @BeforeEach
    void setup() {
        String uniqueEmail = "doctor_" + System.currentTimeMillis() + "@example.com";
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setPassword(password);
        user.setRole(Role.DOCTOR);

        authService.register(user);
        User createdUser = authService.login(uniqueEmail, password);
        this.doctorId = createdUser.getDoctorId();

        Claims claims = Mockito.mock(Claims.class);
        Mockito.when(claims.get("doctorId", Long.class)).thenReturn(doctorId);
        Mockito.when(claims.get("role", String.class)).thenReturn("DOCTOR");
        Mockito.when(claims.get("doctorId")).thenReturn(doctorId);
        Mockito.when(claims.get("role")).thenReturn("DOCTOR");

        Mockito.when(jwtService.extractClaims(Mockito.anyString())).thenReturn(claims);

    }

    @Test
    void getAllDoctorsTest() throws Exception {
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDoctorMeTest() throws Exception {
        mockMvc.perform(get("/api/doctors/me")
                        .header("Authorization", "Bearer dummy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(doctorId));
    }

    @Test
    void updateDoctorProfileTest() throws Exception {
        Doctor updateData = new Doctor();
        updateData.setName("New Doctor Name");

        mockMvc.perform(post("/api/doctors/me")
                        .header("Authorization", "Bearer dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Doctor Name"));
    }

    @Test
    void getDoctorPatientsTest() throws Exception {
        mockMvc.perform(get("/api/doctors/me/patients")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDoctorPendingRequestsTest() throws Exception {
        mockMvc.perform(get("/api/doctors/me/requests")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void approvePatientRequestTest() throws Exception {
        mockMvc.perform(post("/api/doctors/me/approve/1")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").isNumber());
    }

    @Test
    void rejectPatientRequestTest() throws Exception {
        mockMvc.perform(post("/api/doctors/me/reject/1")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").isNumber());
    }

    @Test
    void getSessionsForSpecificPatientTest() throws Exception {
        mockMvc.perform(get("/api/doctors/sessions/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDoctorByIdTest() throws Exception {
        mockMvc.perform(get("/api/doctors/" + doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(doctorId));
    }

    @Test
    void getDoctorPendingRequestsPublicTest() throws Exception {
        mockMvc.perform(get("/api/doctors/" + doctorId + "/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void approvePatientPublicTest() throws Exception {
        mockMvc.perform(post("/api/doctors/" + doctorId + "/approve/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").isNumber());
    }

    @Test
    void rejectPatientPublicTest() throws Exception {
        mockMvc.perform(post("/api/doctors/" + doctorId + "/reject/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").isNumber());
    }

    @Test
    void generateReportTest() throws Exception {
        mockMvc.perform(post("/api/doctors/" + doctorId + "/report/1/generate")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(doctorId));
    }

    @Test
    void getMyReportsTest() throws Exception {
        mockMvc.perform(get("/api/doctors/me/reports")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllLocalitiesTest() throws Exception {
        mockMvc.perform(get("/api/doctors/localities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

}
