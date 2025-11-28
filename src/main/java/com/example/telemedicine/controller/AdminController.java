package com.example.telemedicine.controller;

import com.example.telemedicine.config.OperatorConfig;
import com.example.telemedicine.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * REST controller that provides admin endpoints for managing the server.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final OperatorConfig operatorConfig;


    /**
     * Constructs an AdminController with the specified services and operator configuration.
     *
     * @param adminService   the service responsible for server administration actions
     * @param operatorConfig the configuration object containing operator credentials
     */
    public AdminController(AdminService adminService, OperatorConfig operatorConfig) {
        this.adminService = adminService;
        this.operatorConfig = operatorConfig;
    }

    /**
     * Checks if the provided operator password matches the configured operator password.
     *
     * @param opPass the password to verify
     * @return true if the password matches, false otherwise
     */
    private boolean checkOperatorAuth(String opPass) {
        return operatorConfig.getPassword().equals(opPass);
    }

    /**
     * Stops the server if the provided password is correct.
     *
     * @param body map containing the "password" key
     * @return a ResponseEntity with status 200 and success message if stopped,
     * or error if unauthorized
     */
    @PostMapping("/stop-server")
    public ResponseEntity<?> stopServer(@RequestBody Map<String, String> body) {
        String password = body.get("password");

        if (!checkOperatorAuth(password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            adminService.stop(password);
            return ResponseEntity.ok(Map.of("message", "Server stopped successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Retrieves the current status of the server.
     *
     * @return ResponseEntity containing a map with server status or an error message
     */
    @GetMapping("/server-status")
    public ResponseEntity<?> serverStatus() {

        Map<String, Object> status;
        try {
            status = Map.of("running", adminService.isRunning(), "uptime", adminService.getUptime(), "startTime", adminService.getStartTime(), "memoryUsage", Map.of("used", adminService.getUsedMemory(), "max", adminService.getMaxMemory()), "cpuLoad", adminService.getCpuLoad(), "threadCount", adminService.getThreadCount());
        } catch (Exception e) {
            status = Map.of("error", "Unable to retrieve status: " + e.getMessage());
        }

        return ResponseEntity.ok(status);
    }

    /**
     * Retrieves the contents of the server log file.
     *
     * @return a ResponseEntity containing a map with the "logs" key or an error message
     */
    @GetMapping("/logs")
    public ResponseEntity<?> getLogs() {
        try {
            Path logFile = Paths.get("server.log").toAbsolutePath();

            if (!Files.exists(logFile)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Log file not found"));
            }

            String logs = Files.readString(logFile);
            return ResponseEntity.ok(Map.of("logs", logs));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}