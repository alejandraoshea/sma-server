package com.example.telemedicine.integration;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.repository.PatientRepository;
import com.example.telemedicine.service.AuthService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    private Long patientId;
    private final String userPassword = "password123";

    @BeforeEach
    void setup() {
        String uniqueEmail = "testpatient_" + System.currentTimeMillis() + "@example.com";
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setPassword(userPassword);
        user.setRole(Role.PATIENT);

        authService.register(user);
        User createdUser = authService.login(uniqueEmail, userPassword);
        this.patientId = createdUser.getPatientId();

        Claims claims = Mockito.mock(Claims.class);
        Mockito.when(claims.get("patientId", Long.class)).thenReturn(patientId);
        Mockito.when(claims.get("role", String.class)).thenReturn("PATIENT");
        Mockito.when(jwtService.extractClaims(Mockito.anyString())).thenReturn(claims);
    }

    private long startSessionAndReturnId() throws Exception {
        MvcResult sessionResp = mockMvc.perform(post("/api/patients/sessions/start/me")
                        .header("Authorization", "Bearer dummy")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(sessionResp.getResponse().getContentAsString()).get("sessionId").asLong();
    }

    private void postSymptoms(long sessionId, SymptomType... symptoms) throws Exception {
        Set<SymptomType> s = Set.of(symptoms);
        mockMvc.perform(post("/api/patients/sessions/" + sessionId + "/symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(s)))
                .andExpect(status().isOk());
    }

    private void uploadSignalJson(long sessionId, SignalType type, String data, int fs) throws Exception {
        String json = objectMapper.writeValueAsString(new SignalUploadDto(type.name(), data, fs));
        mockMvc.perform(post("/api/patients/sessions/" + sessionId + "/signals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signalType").value(type.name()));
    }

    static class SignalUploadDto {
        public String signalType;
        public String patientSignalData;
        public Integer fs;
        public SignalUploadDto(String signalType, String patientSignalData, Integer fs) {
            this.signalType = signalType;
            this.patientSignalData = patientSignalData;
            this.fs = fs;
        }
    }

    @Test
    void getPatientByIdTest() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                        .header("Authorization", "Bearer dummy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId));
    }

    @Test
    void selectDoctorFromListTest() throws Exception {
        mockMvc.perform(post("/api/patients/request/4")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").isNumber());
    }

    @Test
    void startSessionTest() throws Exception {
        mockMvc.perform(post("/api/patients/sessions/start/me")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId))
                .andExpect(jsonPath("$.sessionId").isNumber());
    }

    @Test
    void addSymptomsTest() throws Exception {
        long sessionId = startSessionAndReturnId();

        postSymptoms(sessionId, SymptomType.FEVER, SymptomType.HAND_TREMORS);

        mockMvc.perform(get("/api/patients/sessions/" + patientId)
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSymptomEnumTest() throws Exception {
        mockMvc.perform(get("/api/patients/sessions/enum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void addSignalTest() throws Exception {
        long sessionId = startSessionAndReturnId();
        postSymptoms(sessionId, SymptomType.FEVER);
        uploadSignalJson(sessionId, SignalType.ECG, "1,2,3,4", 100);
    }

    @Test
    void getSessionSignalsTest() throws Exception {
        long sessionId = startSessionAndReturnId();
        postSymptoms(sessionId, SymptomType.FEVER);
        uploadSignalJson(sessionId, SignalType.ECG, "1,2,3", 100);

        mockMvc.perform(get("/api/patients/sessions/" + sessionId + "/signals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSessionSymptomsTest() throws Exception {
        long sessionId = startSessionAndReturnId();
        postSymptoms(sessionId, SymptomType.FEVER);

        mockMvc.perform(get("/api/patients/sessions/" + sessionId + "/symptoms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updatePatientTest() throws Exception {
        Patient updateData = new Patient();
        updateData.setName("Updated Name");
        updateData.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/patients/me")
                        .header("Authorization", "Bearer dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void getPatientSessionsTest() throws Exception {
        mockMvc.perform(get("/api/patients/sessions/" + patientId)
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void uploadEcgByteTest() throws Exception {
        long sessionId = startSessionAndReturnId();
        postSymptoms(sessionId, SymptomType.FEVER);

        byte[] dummyBytes = "100\n1,2,3,4".getBytes();
        mockMvc.perform(post("/api/patients/sessions/" + sessionId + "/ecg")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content(dummyBytes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signalType").value("ECG"));
    }

    @Test
    void uploadEmgByteTest() throws Exception {
        long sessionId = startSessionAndReturnId();
        postSymptoms(sessionId, SymptomType.FEVER);

        byte[] dummyBytes = "100\n1,2,3,4".getBytes();
        mockMvc.perform(post("/api/patients/sessions/" + sessionId + "/emg")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content(dummyBytes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signalType").value("EMG"));
    }

    @Test
    void uploadEcgFileTest() throws Exception {
        long sessionId = startSessionAndReturnId();
        postSymptoms(sessionId, SymptomType.FEVER);

        byte[] dummyBytes = "100\n1,2,3,4".getBytes();
        mockMvc.perform(multipart("/api/patients/sessions/" + sessionId + "/ecg")
                        .file("file", dummyBytes)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signalType").value("ECG"));
    }

    @Test
    void uploadEmgFileTest() throws Exception {
        long sessionId = startSessionAndReturnId();
        postSymptoms(sessionId, SymptomType.FEVER);

        byte[] dummyBytes = "100\n1,2,3,4".getBytes();
        mockMvc.perform(multipart("/api/patients/sessions/" + sessionId + "/emg")
                        .file("file", dummyBytes)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signalType").value("EMG"));
    }

    @Test
    void generateAndDownloadSummaryFlowTest() throws Exception {
        long sessionId = startSessionAndReturnId();
        postSymptoms(sessionId, SymptomType.FEVER);

        uploadSignalJson(sessionId, SignalType.ECG, "1,2,3,4", 100);
        uploadSignalJson(sessionId, SignalType.EMG, "1,2,3,4", 100);

        mockMvc.perform(post("/api/patients/sessions/" + sessionId + "/generate-session-file"))
                .andExpect(status().isOk())
                .andExpect(content().string("CSV summary generated and saved successfully."));

        mockMvc.perform(get("/api/patients/sessions/" + sessionId + "/session-file"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"session_" + sessionId + "_file.csv\""))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));
    }

    @Test
    void getDoctorsForMapTest() throws Exception {
        mockMvc.perform(get("/api/patients/me/map-doctors")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSelectedDoctorTest() throws Exception {
        mockMvc.perform(get("/api/patients/me/doctor")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 204, "Expected 200 or 204 but got: " + status);
                    if (status == 200) {
                        String body = result.getResponse().getContentAsString();
                        assertNotNull(body);
                        assertTrue(body.contains("doctorId"));
                    }
                });
    }
}
