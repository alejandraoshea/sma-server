package com.example.telemedicine.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final JdbcTemplate jdbcTemplate;

    public AdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void start() {
        try {

        } catch (Exception e) {
            throw new RuntimeException("Error starting server: " + e.getMessage(), e);
        }
    }

    public void stop() {

    }
}
