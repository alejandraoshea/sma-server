package com.example.telemedicine.integration;

import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientEndpointsTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @Test
    void getPatientById_returnsPatient() throws Exception {
        // Mock the JwtService to return Claims with patientId and role
        Claims mockClaims = Mockito.mock(Claims.class);
        Mockito.when(mockClaims.get("patientId", Long.class)).thenReturn(1L);
        Mockito.when(mockClaims.get("role")).thenReturn("PATIENT");
        Mockito.when(jwtService.extractClaims(Mockito.anyString())).thenReturn(mockClaims);

        MvcResult result = mockMvc.perform(get("/api/patients/me")
                        .header("Authorization", "Bearer dummy-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Patient patient = objectMapper.readValue(jsonResponse, Patient.class);
        assertThat(patient.getPatientId()).isEqualTo(1L);
        assertThat(patient.getName()).isNotBlank();
    }

    @Test
    void updatePatientInfo_updatesSuccessfully() throws Exception {
        Claims mockClaims = Mockito.mock(Claims.class);
        Mockito.when(mockClaims.get("patientId", Long.class)).thenReturn(1L);
        Mockito.when(mockClaims.get("role")).thenReturn("PATIENT");
        Mockito.when(jwtService.extractClaims(Mockito.anyString())).thenReturn(mockClaims);

        Patient update = new Patient();
        update.setName("NewName");

        String jsonRequest = objectMapper.writeValueAsString(update);

        mockMvc.perform(post("/api/patients/me")
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }
}
