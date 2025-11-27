package com.example.telemedicine.integration;

import com.example.telemedicine.repository.DoctorRepository;
import com.example.telemedicine.security.JwtService;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DoctorEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM report WHERE doctor_id = 5 OR patient_id IN (8, 9,10)");
        jdbcTemplate.update("DELETE FROM measurement_sessions WHERE patient_id IN (8, 9,10)");
        jdbcTemplate.update("DELETE FROM patients WHERE patient_id IN (8, 9,10)");
        jdbcTemplate.update("DELETE FROM doctors WHERE doctor_id = 5");
        jdbcTemplate.update("DELETE FROM app_users WHERE user_id IN (200, 201, 150, 202)");

        jdbcTemplate.update("""
            INSERT INTO app_users (user_id, email, password, role) 
            VALUES 
              (150, 'laura.lopez@example.com', 'password', 'DOCTOR'),
              (200, 'alvaro.fernandez@example.com', 'password', 'PATIENT'),
              (201, 'jane.smith@example.com', 'password', 'PATIENT'),
              (202, 'leonor.diez@example.com', 'password', 'PATIENT')
        """);

        jdbcTemplate.update("""
            INSERT INTO doctors (doctor_id, user_id, name, surname, gender)
            VALUES (5, 150, 'Laura', 'Lopez', 'FEMALE')
        """);

        jdbcTemplate.update("""
            INSERT INTO patients (patient_id, user_id, name, surname, gender, birth_date, height, weight,
                  doctor_id, selected_doctor_id, doctor_approval_status)
            VALUES (8, 200, 'Alvaro', 'Fernandez', 'MALE', '1990-01-01', 180, 75,
                  5, 5, 'APPROVED')
        """);

        jdbcTemplate.update("""
            INSERT INTO patients (patient_id, user_id, name, surname, gender, birth_date, height, weight,
                  doctor_id, selected_doctor_id, doctor_approval_status)
            VALUES (9, 201, 'Jane', 'Smith', 'FEMALE', '1999-06-01', 172, 59, 5, 5, 'PENDING')
        """);

        jdbcTemplate.update("""
            INSERT INTO patients (patient_id, user_id, name, surname, gender, birth_date, height, weight,
                  doctor_id, selected_doctor_id, doctor_approval_status)
            VALUES (10, 202, 'Leonor', 'Diez', 'FEMALE', '2003-03-08', 168, 59, 5, 5, 'PENDING')
        """);

        // Insert measurement session for patient 8
        jdbcTemplate.update("""
            INSERT INTO measurement_sessions (session_id, patient_id, time_stamp)
            VALUES (54, 8, NOW())
        """);

        Claims claims = Mockito.mock(Claims.class);
        Mockito.when(claims.get("doctorId", Long.class)).thenReturn(5L);
        Mockito.when(claims.get("role")).thenReturn("DOCTOR");
        Mockito.when(jwtService.extractClaims(Mockito.anyString())).thenReturn(claims);
    }

    @Test
    void getAllDoctorsTest() throws Exception {
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laura"));
    }

    @Test
    void findDoctorByIdTest() throws Exception {
        mockMvc.perform(get("/api/doctors/me")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surname").value("Lopez"));
    }

    @Test
    void getPatientsOfDoctorTest() throws Exception {
        mockMvc.perform(get("/api/doctors/me/patients")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].name").value("Jane"));
    }

    @Test
    void getPendingRequestsTest() throws Exception {
        mockMvc.perform(get("/api/doctors/me/requests")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jane"));
    }

    @Test
    void approvePatientRequestTest() throws Exception {
        mockMvc.perform(post("/api/doctors/me/approve/9")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorApprovalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.doctorId").value(5));
    }

    @Test
    void rejectPatientRequestTest() throws Exception {
        mockMvc.perform(post("/api/doctors/me/reject/10")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorApprovalStatus").value("REJECTED"))
                .andExpect(jsonPath("$.doctorId").value(5));
    }

    @Test
    void getPatientSessionsTest() throws Exception {
        mockMvc.perform(get("/api/doctors/sessions/patients/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionId").value(54));
    }

    //** TODO: token report
    @Test
    void generateReportTest() throws Exception {
        mockMvc.perform(post("/api/doctors/5/report/10/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(5))
                .andExpect(jsonPath("$.sessionId").value(10));
    }

    @Test
    void getReportTest() throws Exception {
        jdbcTemplate.update("""
            INSERT INTO report (report_id, patient_id, doctor_id, session_id,
                file_name, file_type, file_data)
            VALUES (5, 8, 5, 10, 'session10.pdf', 'application/pdf', 'PDFDATA')
        """);

        mockMvc.perform(get("/api/doctors/reports/5"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"session10.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
