package com.example.telemedicine.integration;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.repository.PatientRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        Patient p = new Patient();
        p.setPatientId(1L);
        p.setName("TestName");
        p.setSurname("TestSurname");
        p.setBirthDate(LocalDate.of(2000, 1, 1));
        p.setHeight(175L);
        p.setWeight(70.0);
        patientRepository.updatePatientInfo(1L, p);

        Claims claims = Mockito.mock(Claims.class);
        Mockito.when(claims.get("patientId", Long.class)).thenReturn(1L);
        Mockito.when(claims.get("role")).thenReturn("PATIENT");
        Mockito.when(jwtService.extractClaims(Mockito.anyString())).thenReturn(claims);
    }

    @Test
    void getPatientByIdTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/patients/me")
                        .header("Authorization", "Bearer dummy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andReturn();

        Patient response = objectMapper.readValue(result.getResponse().getContentAsString(), Patient.class);
        assertThat(response.getName()).isEqualTo("TestName");
    }


    @Test
    void updatePatientInfoTest() throws Exception {
        Patient update = new Patient();
        update.setName("UpdatedName");

        mockMvc.perform(post("/api/patients/me")
                        .header("Authorization", "Bearer dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"));
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
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.sessionId").isNumber());
    }

    @Test
    void addSymptomsTest() throws Exception {
        // Create a session first
        MvcResult sessionResp = mockMvc.perform(post("/api/patients/sessions/start/me")
                        .header("Authorization", "Bearer dummy"))
                .andReturn();

        long sessionId =
                objectMapper.readTree(sessionResp.getResponse().getContentAsString())
                        .get("sessionId").asLong();

        Set<SymptomType> symptoms = Set.of(SymptomType.FEVER, SymptomType.HAND_TREMORS);

        mockMvc.perform(post("/api/patients/sessions/" + sessionId + "/symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(symptoms)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").isString());
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
        MvcResult sessionResp = mockMvc.perform(post("/api/patients/sessions/start/me")
                        .header("Authorization", "Bearer dummy"))
                .andReturn();

        long sessionId =
                objectMapper.readTree(sessionResp.getResponse().getContentAsString())
                        .get("sessionId").asLong();

        String signalJson = """
            {
              "signalType": "ECG",
              "patientSignalData": "1,2,3,4",
              "fs": 100
            }
            """;

        mockMvc.perform(post("/api/patients/sessions/" + sessionId + "/signals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signalJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signalType").value("ECG"))
                .andExpect(jsonPath("$.patientSignalData").value("1,2,3,4"));
    }

    @Test
    void getSessionSignalsTest() throws Exception {
        mockMvc.perform(get("/api/patients/sessions/1/signals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSessionSymptomsTest() throws Exception {
        mockMvc.perform(get("/api/patients/sessions/1/symptoms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
