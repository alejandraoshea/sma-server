package com.example.telemedicine.service;

import com.example.telemedicine.domain.User;
import com.example.telemedicine.repository.mapper.UserRowMapper;
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

    public User login(String email, String rawPassword) {
        String sql = "SELECT * FROM public.app_users WHERE email = ?";

        User user;
        try {
            user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);

        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new RuntimeException("Invalid login: user not found");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid login: " + e.getMessage());
        }

        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }
}