package com.example.telemedicine.service;

import com.example.telemedicine.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void register(User user) {
        System.out.println("Registering user: " + user);
        System.out.println("Role as string: " + user.getRole().name() + (user.getRole().name()).getClass());

        String sql = "INSERT INTO public.app_users (email, password, role) VALUES (?, ?, ?)";

        try {
            jdbcTemplate.update(sql,
                    user.getEmail(),
                    passwordEncoder.encode(user.getPassword()),
                    user.getRole().name()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error inserting user: " + e.getMessage(), e);
        }
    }
}