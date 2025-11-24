package com.example.telemedicine.repository.mapper;

import com.example.telemedicine.domain.DoctorApprovalStatus;
import com.example.telemedicine.domain.Gender;
import com.example.telemedicine.domain.Patient;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientRowMapper implements RowMapper<Patient> {
    @Override
    public Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Patient(
                rs.getLong("patient_id"),
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("surname"),
                Gender.valueOf(rs.getString("gender")),
                rs.getDate("birth_date").toLocalDate(),
                rs.getLong("height"),
                rs.getDouble("weight"),
                null, // measurement sessions are loaded separately
                rs.getLong("selected_doctor_id"),
                DoctorApprovalStatus.valueOf(rs.getString("doctor_approval_status"))
        );
    }
}
