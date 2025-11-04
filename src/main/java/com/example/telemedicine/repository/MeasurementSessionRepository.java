package com.example.telemedicine.repository;

import com.example.telemedicine.domain.Signal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SignalRepository {
    private final JdbcTemplate jdbcTemplate;

    public SignalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //** save signal, find patient my id....
}
