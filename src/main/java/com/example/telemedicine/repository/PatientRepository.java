package com.example.telemedicine.repository;

import com.example.telemedicine.domain.Doctor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.telemedicine.domain.SymptomType;
import com.example.telemedicine.domain.Gender;
import com.example.telemedicine.domain.Symptoms;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class PatientRepository {
    private final JdbcTemplate jdbcTemplate;

    public PatientRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Symptoms> findBySessionId(Long sessionId) {
        String sql = "SELECT symptom_id, session_id, symptom_set, time_stamp FROM symptoms WHERE session_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long id = rs.getLong("symptom_id");
            LocalDateTime ts = rs.getTimestamp("time_stamp").toLocalDateTime();
            String[] symptomArray = (String[]) rs.getArray("symptom_set").getArray();
            Set<SymptomType> set = new HashSet<>();
            for (String s : symptomArray) {
                set.add(SymptomType.valueOf(s));
            }
            return new Symptoms(id, sessionId, set, ts);
        }, sessionId);
    }

    public Doctor sendDoctorRequest(Long patientId, Long doctorId){
        String sql = "UPDATE patient SET selected_doctor_id = ?, doctor_approval_status = 'PENDING' WHERE patient_id = ?";
        jdbcTemplate.update(sql, doctorId, patientId);

        // Return doctor
        String docSql = "SELECT doctor_id, name, surname, specialization FROM doctors WHERE doctor_id = ?";
        return jdbcTemplate.queryForObject(docSql, (rs, rowNum) ->
                new Doctor(
                        rs.getLong("doctor_id"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        Gender.valueOf(rs.getString("gender"))
                ), doctorId
        );
    }


}
