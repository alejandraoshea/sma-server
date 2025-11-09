package com.example.telemedicine.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.telemedicine.domain.SymptomType;
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

    public void saveSymptoms(List<Symptoms> symptomsList) {
        if (symptomsList == null || symptomsList.isEmpty()) return;

        String insertSql = "INSERT INTO symptoms (session_id, symptom_set, timestamp) VALUES (?, ?, ?)";

        for (Symptoms symptom : symptomsList) {
            Long sessionId = symptom.getMeasurementSessionId();

            LocalDateTime timestamp;
            if (symptom.getTimestamp() != null) {
                timestamp = symptom.getTimestamp();
            } else {
                timestamp = LocalDateTime.now();
            }

            String[] symptomArray = symptom.getSymptomsSet().stream()
                    .map(Enum::name)
                    .toArray(String[]::new);

            jdbcTemplate.update(insertSql, sessionId, symptomArray, Timestamp.valueOf(timestamp));

        }
    }

    public List<Symptoms> findByPatientId(Long patientId) {
        String sql = """
            SELECT sy.symptom_id, sy.session_id, sy.symptom_set, sy.time_stamp
            FROM symptoms sy
            JOIN measurement_sessions ms ON ms.session_id = sy.session_id
            WHERE ms.patient_id = ?
            ORDER BY sy.time_stamp DESC
        """;
        //** if we want to order it we can add:  "ORDER BY timestamp DESC;"

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long symptomId = rs.getLong("symptom_id");
            Long sessionId = rs.getLong("session_id");
            LocalDateTime ts = rs.getTimestamp("time_stamp").toLocalDateTime();

            // convert PostgreSQL enum[] array → Set<SymptomType>
            String[] symptomArray = (String[]) rs.getArray("symptom_set").getArray();
            Set<SymptomType> symptomSet = new HashSet<>();
            for (String s : symptomArray) {
                symptomSet.add(SymptomType.valueOf(s));
            }

            return new Symptoms(symptomId, sessionId, symptomSet, ts);
        }, patientId);
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

}
