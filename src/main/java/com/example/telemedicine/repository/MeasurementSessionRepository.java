package com.example.telemedicine.repository;

import com.example.telemedicine.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, patientId);
            ps.setTimestamp(2, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        //** getKeys() to fetch all generated keys (instead of just getKey())
        var keys = keyHolder.getKeys();

        Long sessionId = null;
        if (keys != null && keys.containsKey("session_id")) {
            Object keyObj = keys.get("session_id");
            if (keyObj instanceof Number) {
                sessionId = ((Number) keyObj).longValue();
            } else if (keyObj instanceof String) {
                sessionId = Long.parseLong((String) keyObj);
            }
        }

        if (sessionId == null) {
            throw new IllegalStateException("Failed to retrieve session_id from database.");
        }

        return new MeasurementSession(sessionId, patientId, now, null, null);
    }


    public Signal saveSignal(Long sessionId, Signal signal) {
        String selectPatientIdSql = "SELECT patient_id FROM measurement_sessions WHERE session_id = ?";
        Long patientId = jdbcTemplate.queryForObject(selectPatientIdSql, Long.class, sessionId);

        if (patientId == null) {
            throw new IllegalStateException("No patient found for sessionId: " + sessionId);
        }

        String sql = """
            INSERT INTO signals (patient_id, session_id, time_stamp, signal_type, patient_data)
            VALUES (?, ?, ?, ?, ?)
            """;

        LocalDateTime timestamp;
        if (signal.getTimestamp() != null) {
            timestamp = signal.getTimestamp();
        } else {
            timestamp = LocalDateTime.now();
        }

        jdbcTemplate.update(sql, patientId, sessionId, Timestamp.valueOf(timestamp),
                signal.getSignalType().name(), signal.getPatientSignalData()
        );

        signal.setTimestamp(timestamp);
        return signal;
    }

    public Symptoms saveSymptoms(Long sessionId, Symptoms symptoms) {
        String selectPatientIdSql = "SELECT patient_id FROM measurement_sessions WHERE session_id = ?";
        Long patientId = jdbcTemplate.queryForObject(selectPatientIdSql, Long.class, sessionId);

        if (patientId == null) {
            throw new IllegalStateException("No patient found for sessionId: " + sessionId);
        }

        String insertSql = """
            INSERT INTO symptoms (patient_id, session_id, time_stamp, patient_data, symptom_set)
            VALUES (?, ?, ?, ?, ?::symptoms_enum[])
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

        jdbcTemplate.update(insertSql, patientId, sessionId, Timestamp.valueOf(timestamp), null, symptomArray); //** check for patientdata future

        symptoms.setTimestamp(timestamp);
        return symptoms;
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
                rs.getString("patient_signal_data"),
                rs.getInt(0) //change to fs
        ), sessionId);
    }

    public List<Symptoms> findSymptomsBySessionId(Long sessionId) {
        String sql = """
            SELECT sy.symptom_id, sy.session_id, sy.symptom_set, sy.time_stamp
            FROM symptoms sy
            WHERE session_id = ?
            ORDER BY time_stamp DESC
        """;
        //** if we want to order it we can add:  "ORDER BY timestamp DESC;"

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long symptomId = rs.getLong("symptom_id");
            LocalDateTime ts = rs.getTimestamp("time_stamp").toLocalDateTime();

            // convert PostgreSQL enum[] array → Set<SymptomType>
            String[] symptomArray = (String[]) rs.getArray("symptom_set").getArray();
            Set<SymptomType> symptomSet = new HashSet<>();
            for (String s : symptomArray) {
                symptomSet.add(SymptomType.valueOf(s));
            }

            return new Symptoms(symptomId, sessionId, symptomSet, ts);
        }, sessionId);
    }

    /**
     * This method gets the measurement history (sessions) for a selected patient
     * @param patientId the patient id corresponding to the patient from who we want to see the historial as integer
     * @return the measurement session of the patient as list
     */
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


    //! todo: by date
    /**
     * This method gets the measurement history (sessions) for a selected patient
     * @param sessionDate the patient id corresponding to the patient from who we want to see the historial as integer
     * @return the measurement session of the patient as list
     */
    public List<MeasurementSession> findSessionsByDate(LocalDateTime sessionDate) {
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
        }, sessionDate);
    }

    //!! TODO verify that when the doctor sees all patients (only his patients)

    public Signal addEMGToMeassurementSession(byte[] fileBytes, Long sessionId) throws IOException {
        String selectPatientIdSql = "SELECT patient_id FROM measurement_sessions WHERE session_id = ?";
        Long patientId = jdbcTemplate.queryForObject(selectPatientIdSql, Long.class, sessionId);

        if (patientId == null) {
            throw new IllegalStateException("No patient found for sessionId: " + sessionId);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileBytes)));

        String firstLine = null;
        String data = null;
        try {
            firstLine = reader.readLine();
            data = reader.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("EMG signal file is empty");
        }

        int fs = Integer.parseInt(firstLine.trim());

        //TODO PROCESAMIENTO DE SEÑAL AQUI (de data)

        String sql = """
            INSERT INTO signals (patient_id, session_id, time_stamp, signal_type, patient_data, fs)
            VALUES (?, ?, ?, ?, ?)
            """;

        LocalDateTime timestamp = LocalDateTime.now();
        jdbcTemplate.update(sql, patientId, sessionId, Timestamp.valueOf(timestamp), SignalType.EMG, data);

        return new Signal(patientId, sessionId, timestamp, SignalType.EMG, data, fs);

    }

    public Signal addECGToMeassurementSession(byte[] fileBytes, Long sessionId) {
        String selectPatientIdSql = "SELECT patient_id FROM measurement_sessions WHERE session_id = ?";
        Long patientId = jdbcTemplate.queryForObject(selectPatientIdSql, Long.class, sessionId);

        if (patientId == null) {
            throw new IllegalStateException("No patient found for sessionId: " + sessionId);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileBytes)));

        String firstLine = null;
        String data = null;
        try {
            firstLine = reader.readLine();
            data = reader.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("ECG signal file is empty");
        }

        int fs = Integer.parseInt(firstLine.trim());

        //TODO PROCESAMIENTO DE SEÑAL AQUI (de data)

        String sql = """
            INSERT INTO signals (patient_id, session_id, time_stamp, signal_type, patient_data, sampling_rate)
            VALUES (?, ?, ?, ?, ?)
            """;

        LocalDateTime timestamp = LocalDateTime.now();
        jdbcTemplate.update(sql, patientId, sessionId, Timestamp.valueOf(timestamp), SignalType.ECG, data);

        return new Signal(patientId, sessionId, timestamp, SignalType.ECG, data, fs);
    }
}
