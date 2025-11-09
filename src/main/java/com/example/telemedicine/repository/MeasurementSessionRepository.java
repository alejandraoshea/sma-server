package com.example.telemedicine.repository;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.Symptoms;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MeasurementSessionRepository {
    private final JdbcTemplate jdbcTemplate;

    public MeasurementSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MeasurementSession startNewSession(Long patientId) {
        String sql = """
            INSERT INTO measurement_sessions (patient_id, time_stamp)
            VALUES (?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder(); //to capture the session_id
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, patientId);
            ps.setTimestamp(2, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Long sessionId = keyHolder.getKey().longValue();
        return new MeasurementSession(sessionId, patientId, now, null, null);
    }

    public Signal saveSignal(Long sessionId, Signal signal) {
        String sql = """
            INSERT INTO signals (session_id, time_stamp, signal_type, patient_data)
            VALUES (?, ?, ?, ?, ?)
            """;

        LocalDateTime timestamp;
        if (signal.getTimestamp() != null) {
            timestamp = signal.getTimestamp();
        } else {
            timestamp = LocalDateTime.now();
        }

        jdbcTemplate.update(sql, sessionId, Timestamp.valueOf(timestamp),
                signal.getSignalType().name(), signal.getPatientSignalData()
        );

        signal.setTimestamp(timestamp);
        return signal;
    }

    public Symptoms saveSymptoms(Long sessionId, Symptoms symptoms) {
        String insertSql = """
        INSERT INTO symptoms (session_id, time_stamp, patient_data, symptom_set)
        VALUES (?, ?, ?, ?)
        """;

        LocalDateTime timestamp;
        if (symptoms.getTimestamp() != null) {
            timestamp = symptoms.getTimestamp();
        } else {
            timestamp = LocalDateTime.now();
        }

        String[] symptomArray = symptoms.getSymptomsSet().stream()
                .map(Enum::name)
                .toArray(String[]::new);

        jdbcTemplate.update(insertSql, sessionId, Timestamp.valueOf(timestamp), symptomArray); //** check for patientdata future

        symptoms.setTimestamp(timestamp);
        return symptoms;
    }


    public List<MeasurementSession> findByPatientId(Long patientId) {
        String sql = """
            SELECT session_id, patient_id, time_stamp
            FROM measurement_sessions
            WHERE patient_id = ?
            ORDER BY time_stamp DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long sessionId = rs.getLong("session_id");
            Long pId = rs.getLong("patient_id");
            LocalDateTime ts = rs.getTimestamp("time_stamp").toLocalDateTime();
            return new MeasurementSession(sessionId, pId, ts, null, null);
        }, patientId);
    }


    public List<Signal> findSignalsBySessionId(Long sessionId) {
        String sql = """
            SELECT signal_id, session_id, patient_id, time_stamp, signal_type, patient_signal_data
            FROM signals
            WHERE session_id = ?
            ORDER BY time_stamp
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Signal(
                rs.getLong("signal_id"),
                rs.getLong("session_id"),
                rs.getTimestamp("time_stamp").toLocalDateTime(),
                Enum.valueOf(com.example.telemedicine.domain.SignalType.class, rs.getString("signal_type")),
                rs.getString("patient_signal_data")
        ), sessionId);
    }

    public MeasurementSession findSessionById(Long sessionId) {
        String sql = """
            SELECT session_id, patient_id, time_stamp
            FROM measurement_sessions
            WHERE session_id = ?
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new MeasurementSession(
                        rs.getLong("session_id"),
                        rs.getLong("patient_id"),
                        rs.getTimestamp("time_stamp").toLocalDateTime(),
                        null,
                        null
                ), sessionId);
    }

    public List<MeasurementSession> findSessionsByPatientId(Long patientId) {
        String sql = """
            SELECT session_id, patient_id, time_stamp
            FROM measurement_sessions
            WHERE patient_id = ?
            ORDER BY time_stamp DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long sessionId = rs.getLong("session_id");
            Long pid = rs.getLong("patient_id");
            LocalDateTime timestamp = rs.getTimestamp("time_stamp").toLocalDateTime();
            return new MeasurementSession(sessionId, pid, timestamp, null, null);
        }, patientId);
    }



}
