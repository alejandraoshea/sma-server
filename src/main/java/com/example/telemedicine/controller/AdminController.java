package com.example.telemedicine.controller;

import com.example.telemedicine.config.OperatorConfig;
import com.example.telemedicine.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private boolean checkOperatorAuth(String opUser, String opPass) {
        return operatorConfig.getUsername().equals(opUser) &&
                operatorConfig.getPassword().equals(opPass);
    }

    @PostMapping("/start-server")
    public ResponseEntity<?> startServer(
            @RequestHeader("X-OP-USER") String opUser,
            @RequestHeader("X-OP-PASS") String opPass) {

        if (!checkOperatorAuth(opUser, opPass)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        adminService.start();
        return ResponseEntity.ok(Map.of("message", "Server started successfully"));
    }

    @PostMapping("/stop-server")
    public ResponseEntity<?> stopServer(
            @RequestHeader("X-OP-USER") String opUser,
            @RequestHeader("X-OP-PASS") String opPass) {

        if (!checkOperatorAuth(opUser, opPass)) {
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
    public ResponseEntity<?> serverStatus(
            @RequestHeader("X-OP-USER") String opUser,
            @RequestHeader("X-OP-PASS") String opPass) {

        if (!checkOperatorAuth(opUser, opPass)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Map<String, Object> status = Map.of(
                "running", adminService.isRunning(),
                "pid", adminService.getPid(),
                "uptime", adminService.getUptime(),
                "startTime", adminService.getStartTime(),
                "memoryUsage", Map.of(
                        "used", adminService.getUsedMemory(),
                        "free", adminService.getFreeMemory(),
                        "max", adminService.getMaxMemory()
                ),
                "cpuLoad", adminService.getCpuLoad(),
                "threadCount", adminService.getThreadCount(),
                "dbConnections", adminService.getDbConnectionCount()
        );

        return ResponseEntity.ok(status);
    }
}