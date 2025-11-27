package com.example.telemedicine.repository;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.signal.*;
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
import java.util.*;


@Repository
public class PatientRepository {
    private final JdbcTemplate jdbcTemplate;

    public PatientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Finds the patient ID associated with a given user ID.
     *
     * @param userId ID of the user
     * @return The corresponding patient ID, or null if not found
     */
    public Long findPatientIdByUserId(Long userId) {
        try {
            String sql = "SELECT patient_id FROM patients WHERE user_id = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, userId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Updates patient's personal information in the database using SQL.
     *
     * @param patientId ID of the patient to update
     * @param newData   Patient object containing new data (null fields are ignored)
     * @return number of rows updated (should be 1 if successful)
     */
    public int updatePatientInfo(Long patientId, Patient newData) {
        String sql = """
                    UPDATE patients
                    SET
                        name = COALESCE(?, name),
                        surname = COALESCE(?, surname),
                        gender = COALESCE(?::gender_enum, gender),
                        birth_date = COALESCE(?, birth_date),
                        height = COALESCE(?, height),
                        weight = COALESCE(?, weight)
                    WHERE patient_id = ?
                """;

        return jdbcTemplate.update(sql,
                newData.getName(),
                newData.getSurname(),
                newData.getGender() != null ? newData.getGender().name() : null,
                newData.getBirthDate() != null ? java.sql.Date.valueOf(newData.getBirthDate()) : null,
                newData.getHeight(),
                newData.getWeight(),
                patientId
        );
    }

    /**
     * Finds and returns a patient by its unique identifier.
     *
     * @param patientId Database primary key of the patient.
     * @return Patient object.
     * @throws org.springframework.dao.EmptyResultDataAccessException if no patient exists.
     */
    public Patient findById(Long patientId) {
        String sql = """
                    SELECT *
                    FROM patients
                    WHERE patient_id = ?
                """;


        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Patient p = new Patient();
            p.setPatientId(rs.getLong("patient_id"));
            p.setUserId(rs.getLong("user_id"));
            p.setName(rs.getString("name"));
            p.setSurname(rs.getString("surname"));

            String genderStr = rs.getString("gender");
            p.setGender(genderStr != null ? Gender.valueOf(genderStr) : null);

            p.setBirthDate(rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null);
            p.setHeight(Long.valueOf(rs.getObject("height") != null ? rs.getInt("height") : 0));
            p.setWeight(rs.getObject("weight") != null ? rs.getInt("weight") : 0);
            p.setDoctorId(rs.getObject("doctor_id") != null ? rs.getLong("doctor_id") : null);
            p.setSelectedDoctorId(rs.getObject("selected_doctor_id") != null ? rs.getLong("selected_doctor_id") : null);

            String statusStr = rs.getString("doctor_approval_status");
            p.setDoctorApprovalStatus(statusStr != null ? DoctorApprovalStatus.valueOf(statusStr) : null);

            return p;
        }, patientId);
    }

    /**
     * Assigns a doctor to a patient and marks the doctor approval status as PENDING.
     *
     * @param patientId ID of the patient who sent the request.
     * @param doctorId  ID of the requested doctor.
     * @return The doctor object that was assigned.
     */
    public Doctor sendDoctorRequest(Long patientId, Long doctorId) {
        String sql = """
                    UPDATE patients
                    SET selected_doctor_id = ?, doctor_approval_status = 'PENDING'
                    WHERE patient_id = ?
                """;

        jdbcTemplate.update(sql, doctorId, patientId);

        // return doctor object
        String docSql = """
                    SELECT doctor_id, name, surname, gender
                    FROM doctors
                    WHERE doctor_id = ?
                """;

        return jdbcTemplate.queryForObject(docSql, (rs, rowNum) -> {
            String genderStr = rs.getString("gender");
            Gender gender = (genderStr != null) ? Gender.valueOf(genderStr) : null;
            return new Doctor(
                    rs.getLong("doctor_id"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    gender
            );
        }, doctorId);
    }

    /**
     * Creates a new measurement session for a patient.
     *
     * @param patientId ID of the patient starting the session.
     * @return A MeasurementSession representing the new session.
     * @throws IllegalStateException if the session ID cannot be retrieved (DB misconfiguration).
     */
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

    /**
     * Saves symptoms directly into the measurement_sessions table.
     *
     * @param sessionId The ID of the session to update.
     * @param symptoms  The set of symptoms.
     * @return The saved set of SymptomType.
     * @throws IllegalArgumentException if symptoms or its set is null.
     */
    public Set<SymptomType> saveSymptoms(Long sessionId, Set<SymptomType> symptoms) {
        if (symptoms == null) {
            throw new IllegalArgumentException("Symptoms set cannot be null.");
        }

        String sql = """
                UPDATE measurement_sessions
                SET symptoms = ?::symptoms_enum[]
                WHERE session_id = ?
                """;

        String[] symptomArray = symptoms.stream()
                .map(Enum::name)
                .toArray(String[]::new);

        jdbcTemplate.update(sql, (Object) symptomArray, sessionId);

        return symptoms;
    }

    /**
     * Saves a basic (text-based) patient signal into the database.
     *
     * @param sessionId The session to attach the signal to.
     * @param signal    The signal to store.
     * @return The updated signal with timestamp set.
     * @throws IllegalStateException if the session could not be matched to a patient.
     */
    public Signal saveSignal(Long sessionId, Signal signal) {
        String selectPatientIdSql = "SELECT patient_id FROM measurement_sessions WHERE session_id = ?";
        Long patientId = jdbcTemplate.queryForObject(selectPatientIdSql, Long.class, sessionId);

        if (patientId == null) {
            throw new IllegalStateException("No patient found for sessionId: " + sessionId);
        }

        String sql = """
                INSERT INTO signals (session_id, time_stamp, signal_type, patient_data)
                VALUES (?, ?, ?::signal_type_enum, ?)
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


    /**
     * Retrieves all signals belonging to a session
     *
     * @param sessionId ID of the session.
     * @return List of Signal ordered chronologically.
     */
    public List<Signal> findSignalsBySessionId(Long sessionId) {
        String sql = "SELECT signal_id, session_id, time_stamp, patient_data, fs, signal_type FROM signals WHERE session_id = ? ORDER BY time_stamp";
        return jdbcTemplate.query(sql, new Object[]{sessionId}, (rs, rowNum) ->
             new Signal(
                    rs.getLong("signal_id"),
                    rs.getLong("session_id"),
                    rs.getTimestamp("time_stamp").toLocalDateTime(),
                    SignalType.valueOf(rs.getString("signal_type")),
                    rs.getString("patient_data"),
                    rs.getInt("fs")
             ));
    }


    /**
     * Retrieves all symptoms recorded in a specific measurement session
     *
     * @param sessionId ID of the session to query.
     * @return Set of SymptomType enums for that session. Empty set if none exist.
     */
    public Set<SymptomType> findSymptomsBySessionId(Long sessionId) {
        String sql = "SELECT symptoms FROM measurement_sessions WHERE session_id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            java.sql.Array symptomsArray = rs.getArray("symptoms");
            Set<SymptomType> symptomSet = new HashSet<>();
            if (symptomsArray != null) {
                String[] symptomsDb = (String[]) symptomsArray.getArray();
                for (String s : symptomsDb) {
                    symptomSet.add(SymptomType.valueOf(s));
                }
            }
            return symptomSet;
        }, sessionId);
    }


    /**
     * This method gets the measurement history (sessions) for a selected patient
     *
     * @param patientId the patient id corresponding to the patient from who we want to see the historial as integer
     * @return the measurement session of the patient as list
     */
    public List<MeasurementSession> findSessionsByPatientId(Long patientId) {
        String sql = """
                    SELECT session_id, patient_id, time_stamp, symptoms
                    FROM measurement_sessions
                    WHERE patient_id = ?
                    ORDER BY time_stamp DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long sessionId = rs.getLong("session_id");
            Long pid = rs.getLong("patient_id");
            LocalDateTime timestamp = rs.getTimestamp("time_stamp").toLocalDateTime();

            java.sql.Array symptomsArray = rs.getArray("symptoms");
            Set<SymptomType> symptomSet = new HashSet<>();
            if (symptomsArray != null) {
                String[] symptomsDb = (String[]) symptomsArray.getArray();
                for (String s : symptomsDb) {
                    symptomSet.add(SymptomType.valueOf(s));
                }
            }

            return new MeasurementSession(sessionId, pid, timestamp, symptomSet, null);
        }, patientId);
    }

    /**
     * Retrieves a measurement session by its id
     * @param sessionId The id of the session
     * @return Measurement Session
     */
    public MeasurementSession findSessionsById(Long sessionId) {
        String sql = """
                SELECT patient_id, time_stamp, symptoms
                FROM measurement_sessions
                WHERE session_id = ?
                ORDER BY time_stamp DESC
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Long pid = rs.getLong("patient_id");
            LocalDateTime timestamp = rs.getTimestamp("time_stamp").toLocalDateTime();
            java.sql.Array symptomsArray = rs.getArray("symptoms");
            Set<SymptomType> symptomSet = new HashSet<>();
            if (symptomsArray != null) {
                String[] symptomsDb = (String[]) symptomsArray.getArray();
                for (String s : symptomsDb) {
                    symptomSet.add(SymptomType.valueOf(s));
                }
            }
            return new MeasurementSession(sessionId, pid, timestamp, symptomSet);
        }, sessionId);
    }

    /**
     * Retrieves all measurement sessions that occurred on a specific date.
     *
     * @param sessionDate The date to filter sessions by.
     * @return List of MeasurementSession objects.
     */
    public List<MeasurementSession> findSessionsByDate(LocalDateTime sessionDate) {
        String sql = """
                SELECT session_id, patient_id, time_stamp
                FROM measurement_sessions
                WHERE DATE(time_stamp) = DATE(?)
                ORDER BY time_stamp DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long sessionId = rs.getLong("session_id");
            Long pid = rs.getLong("patient_id");
            LocalDateTime timestamp = rs.getTimestamp("time_stamp").toLocalDateTime();

            return new MeasurementSession(sessionId, pid, timestamp, null, null);
        }, sessionDate);
    }


    /**
     * Reads an uploaded EMG file (expected format: first line = sampling rate,
     * second line = raw data), validates it, and stores it in the database.
     *
     * @param fileBytes Raw file content.
     * @param sessionId Session ID to attach the signal to.
     * @return Saved {@link Signal} metadata.
     * @throws IllegalStateException if the file is malformed or session is invalid.
     */
    public Signal addEMG(byte[] fileBytes, Long sessionId) {
        String selectPatientIdSql = "SELECT patient_id FROM measurement_sessions WHERE session_id = ?";
        Long patientId = jdbcTemplate.queryForObject(selectPatientIdSql, Long.class, sessionId);

        if (patientId == null) {
            throw new IllegalStateException("No patient found for sessionId: " + sessionId);
        }

        Signal parsed = SignalProcessing.parseSignalFile(fileBytes, SignalType.EMG, sessionId);

        double[] raw = SignalProcessing.stringToDoubleArray(parsed.getPatientSignalData());
        double[] mv = SignalProcessing.convertToMV(raw, 3.0, 10, 1000);

        double[] bandpassed = SignalProcessing.bandpassFilter(mv, parsed.getFs(), 50, 300, 4);
        double[] finalFiltered = SignalProcessing.notchFilter(bandpassed, parsed.getFs(), 60, 30);

        ContractionResult cr = EMGProcessor.detectContractions(finalFiltered, parsed.getFs(), 0.165, 0.10);

        String finalData = SignalProcessing.doubleArrayToString(finalFiltered);

        String sql = """
                    INSERT INTO signals (session_id, time_stamp, signal_type, patient_data, fs)
                    VALUES (?, ?, ?::signal_type_enum, ?, ?)
                """;

        LocalDateTime timestamp = LocalDateTime.now();
        KeyHolder key = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, sessionId);
            ps.setTimestamp(2, Timestamp.valueOf(timestamp));
            ps.setString(3, SignalType.EMG.name());
            ps.setString(4, finalData);
            ps.setInt(5, parsed.getFs());
            return ps;
        }, key);

        Long signalId = ((Number) key.getKeys().get("signal_id")).longValue();

        return new Signal(signalId, sessionId, timestamp, SignalType.EMG, finalData, parsed.getFs());
    }


    /**
     * Reads an uploaded ECG file (expected format: first line = sampling rate,
     * second line = raw signal values), validates it, and stores it.
     *
     * @param fileBytes Raw file content.
     * @param sessionId Session ID to attach the signal to.
     * @return A {@link Signal} instance containing sampling rate & metadata.
     * @throws IllegalStateException if file is empty or session invalid.
     */
    public Signal addECG(byte[] fileBytes, Long sessionId) {
        String selectPatientIdSql = "SELECT patient_id FROM measurement_sessions WHERE session_id = ?";
        Long patientId = jdbcTemplate.queryForObject(selectPatientIdSql, Long.class, sessionId);

        if (patientId == null) {
            throw new IllegalStateException("No patient found for sessionId: " + sessionId);
        }

        Signal parsed = SignalProcessing.parseSignalFile(fileBytes, SignalType.ECG, sessionId);

        double[] raw = SignalProcessing.stringToDoubleArray(parsed.getPatientSignalData());
        double[] mvSignal = SignalProcessing.convertToMV(raw, 3.3, 10, 1100);
        double[] filtered = ECGProcessor.applyFilters(mvSignal, parsed.getFs());

        QRSResult qrs = ECGProcessor.detectQRSComplexes(filtered, parsed.getFs());

        String finalData = SignalProcessing.doubleArrayToString(filtered);
        String sql = """
                    INSERT INTO signals (session_id, time_stamp, signal_type, patient_data, fs)
                    VALUES (?, ?, ?::signal_type_enum, ?, ?)
                """;

        LocalDateTime timestamp = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, sessionId);
            ps.setTimestamp(2, Timestamp.valueOf(timestamp));
            ps.setString(3, SignalType.ECG.name());
            ps.setString(4, finalData);
            ps.setInt(5, parsed.getFs());
            return ps;
        }, keyHolder);

        Long signalId = ((Number) keyHolder.getKeys().get("signal_id")).longValue();

        return new Signal(signalId, sessionId, timestamp, SignalType.ECG, finalData, parsed.getFs());
    }

    public void saveCsvSummaryFile(Long sessionId, byte[] csvBytes, String filename, String mimeType) {
        String sql = """
        UPDATE measurement_sessions
        SET session_file = ?, session_filename = ?, session_mime_type = ? 
        WHERE session_id = ?
        """;
        jdbcTemplate.update(sql, csvBytes, filename, mimeType, sessionId);
    }

    public byte[] getCsvSummaryFile(Long sessionId) {
        String sql = "SELECT session_file FROM measurement_sessions WHERE session_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                rs.getBytes("session_file"),
                sessionId);
    }

}
