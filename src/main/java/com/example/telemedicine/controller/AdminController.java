package com.example.telemedicine.controller;

import com.example.telemedicine.config.OperatorConfig;
import com.example.telemedicine.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final OperatorConfig operatorConfig;

    public AdminController(AdminService adminService, OperatorConfig operatorConfig) {
        this.adminService = adminService;
        this.operatorConfig = operatorConfig;
    }

    private boolean checkOperatorAuth(String opPass) {
        return operatorConfig.getPassword().equals(opPass);
    }

    @PostMapping("/stop-server")
    public ResponseEntity<?> stopServer(@RequestHeader("X-OP-PASS") String opPass) {

        if (!checkOperatorAuth(opPass)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            adminService.stop();
            return ResponseEntity.ok(Map.of("message", "Server stopped successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/server-status")
    public ResponseEntity<?> serverStatus() {

        Map<String, Object> status;
        try {
            status = Map.of("running", adminService.isRunning(), "uptime", adminService.getUptime(), "startTime", adminService.getStartTime(), "memoryUsage", Map.of("used", adminService.getUsedMemory(), "free", adminService.getFreeMemory(), "max", adminService.getMaxMemory()), "cpuLoad", adminService.getCpuLoad(), "threadCount", adminService.getThreadCount());
        } catch (Exception e) {
            status = Map.of("error", "Unable to retrieve status: " + e.getMessage());
        }

        return ResponseEntity.ok(status);
    }

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