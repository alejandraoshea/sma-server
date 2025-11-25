package com.example.telemedicine.service;

import com.example.telemedicine.domain.Role;
import com.example.telemedicine.domain.User;
import com.example.telemedicine.repository.mapper.UserRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;

/**
 * Service for handling user authentication and registration.
 * Provides methods for registering new users and logging in existing users.
 * Handles password hashing using BCrypt and manages user roles.
 */
@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Constructs an AuthService with the provided JdbcTemplate.
     *
     * @param jdbcTemplate the JdbcTemplate for database access
     */
    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Registers a new user in the system.
     * Inserts the user into the app_users table with a hashed password.
     * Depending on the user's role, inserts a corresponding record into
     * the patients or doctors table.
     *
     * @param user the User object containing email, password, and role
     * @throws RuntimeException if an error occurs during database insertion
     */
    public void register(User user) {
        try {
            String sql = "INSERT INTO public.app_users (email, password, role) VALUES (?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
                ps.setString(1, user.getEmail());
                ps.setString(2, passwordEncoder.encode(user.getPassword()));
                ps.setString(3, user.getRole().name());
                return ps;
            }, keyHolder);

            int userId = keyHolder.getKey().intValue();

            if (user.getRole() == Role.PATIENT) {
                String insertPatientSql = "INSERT INTO patients (user_id) VALUES (?)";
                jdbcTemplate.update(insertPatientSql, userId);
            } else if (user.getRole() == Role.DOCTOR) {
                String insertDoctorSql = "INSERT INTO doctors (user_id) VALUES (?)";
                jdbcTemplate.update(insertDoctorSql, userId);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error inserting user: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates a user by email and password.
     * Retrieves the user from the database and verifies the password using BCrypt.
     *
     * @param email       the user's email
     * @param rawPassword the user's raw password input
     * @return the authenticated User object
     * @throws RuntimeException if the user is not found or the password does not match
     */
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