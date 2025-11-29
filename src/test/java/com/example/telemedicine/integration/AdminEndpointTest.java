package com.example.telemedicine.integration;

import com.example.telemedicine.controller.AdminController;
import com.example.telemedicine.service.AdminService;
import com.example.telemedicine.config.OperatorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class AdminEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private OperatorConfig operatorConfig;

    private final String correctPassword = "secret123";
    private final String wrongPassword = "wrongpass";

    @BeforeEach
    void setup() {
        when(operatorConfig.getPassword()).thenReturn(correctPassword);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void stopServer_Authorized_Success() throws Exception {
        doNothing().when(adminService).stop(correctPassword);

        mockMvc.perform(post("/api/admin/stop-server")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + correctPassword + "\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Server stopped successfully"));

        verify(adminService, times(1)).stop(correctPassword);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void stopServer_Unauthorized_WrongPassword() throws Exception {
        mockMvc.perform(post("/api/admin/stop-server")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + wrongPassword + "\"}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(adminService, times(0)).stop(anyString());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void stopServer_BadRequest_Exception() throws Exception {
        doThrow(new RuntimeException("Stop failed")).when(adminService).stop(correctPassword);

        mockMvc.perform(post("/api/admin/stop-server")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + correctPassword + "\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Stop failed"));

        verify(adminService, times(1)).stop(correctPassword);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void serverStatus_Success() throws Exception {
        when(adminService.isRunning()).thenReturn(true);
        when(adminService.getUptime()).thenReturn("01h 10m 30s");
        when(adminService.getStartTime()).thenReturn(java.time.Instant.now());
        when(adminService.getUsedMemory()).thenReturn(123456L);
        when(adminService.getMaxMemory()).thenReturn(999999L);
        when(adminService.getCpuLoad()).thenReturn(42.5);
        when(adminService.getThreadCount()).thenReturn(10);

        mockMvc.perform(get("/api/admin/server-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(true))
                .andExpect(jsonPath("$.uptime").value("01h 10m 30s"))
                .andExpect(jsonPath("$.memoryUsage.used").value(123456))
                .andExpect(jsonPath("$.memoryUsage.max").value(999999))
                .andExpect(jsonPath("$.cpuLoad").value(42.5))
                .andExpect(jsonPath("$.threadCount").value(10));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void serverStatus_Error() throws Exception {
        when(adminService.isRunning()).thenThrow(new RuntimeException("Error fetching status"));

        mockMvc.perform(get("/api/admin/server-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unable to retrieve status: Error fetching status"));
    }

}
