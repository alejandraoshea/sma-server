package com.example.telemedicine.controller;

import com.example.telemedicine.config.OperatorConfig;
import com.example.telemedicine.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/start-server")
    public ResponseEntity<?> startServer(
            @RequestHeader("X-OP-USER") String opUser,
            @RequestHeader("X-OP-PASS") String opPass) {

        if (!operatorConfig.getUsername().equals(opUser) ||
                !operatorConfig.getPassword().equals(opPass)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        adminService.start();
        return ResponseEntity.ok(Map.of("message", "Server started successfully"));
    }

    @PostMapping("/stop-server")
    public ResponseEntity<?> stopServer(
            @RequestHeader("X-OP-USER") String opUser,
            @RequestHeader("X-OP-PASS") String opPass) {

        if (!operatorConfig.getUsername().equals(opUser) ||
                !operatorConfig.getPassword().equals(opPass)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            adminService.stop();
            return ResponseEntity.ok(Map.of("message", "Server stopped successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}