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
        String genderStr = rs.getString("gender");
        Gender gender = (genderStr != null && !genderStr.isBlank()) ? Gender.valueOf(genderStr) : null;

        String statusStr = rs.getString("doctor_approval_status");
        DoctorApprovalStatus status = (statusStr != null) ? DoctorApprovalStatus.valueOf(statusStr) : null;

        Long doctorId = rs.getObject("doctor_id") != null ? rs.getLong("doctor_id") : null;
        Long selectedDoctorId = rs.getObject("selected_doctor_id") != null ? rs.getLong("selected_doctor_id") : null;

        return new Patient(
                rs.getLong("patient_id"),
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("surname"),
                gender,
                rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null,
                rs.getLong("height"),
                rs.getDouble("weight"),
                null, // measurement sessions loaded separately
                doctorId,
                selectedDoctorId,
                status
        );
    }
}
