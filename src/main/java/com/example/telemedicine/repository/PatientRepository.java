package com.example.telemedicine.repository;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.signal.ECGProcessor;
import com.example.telemedicine.signal.EMGProcessor;
import com.example.telemedicine.signal.SignalUtils;
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

    public PatientRepository(JdbcTemplate jdbcTemplate){
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
     * Retrieves all symptoms recorded in a specific measurement session
     * @param sessionId ID of the session to query.
     * @return List of Symptoms objects for that session. Empty list if none exist.
     */
    public List<Symptoms> findBySessionId(Long sessionId) {
        String sql = "SELECT symptom_id, session_id, symptom_set, time_stamp FROM measurement_session WHERE session_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long id = rs.getLong("symptom_id");
            LocalDateTime ts = rs.getTimestamp("time_stamp").toLocalDateTime();
            String[] symptomArray = (String[]) rs.getArray("symptom_set").getArray();
            Set<SymptomType> symptomsSet = new HashSet<>();
            for (String symptom : symptomArray) {
                symptomsSet.add(SymptomType.valueOf(symptom));
            }
            return new Symptoms(id, sessionId, symptomsSet, ts);
        }, sessionId);
    }

    /**
     * Finds and returns a patient by its unique identifier.
     * @param patientId Database primary key of the patient.
     * @return Patient object.
     * @throws org.springframework.dao.EmptyResultDataAccessException if no patient exists.
     */
    public Patient findById(Long patientId) {
        String sql = """
            SELECT patient_id, name, surname, selected_doctor_id, doctor_approval_status
            FROM patients
            WHERE patient_id = ?
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Patient p = new Patient();
            p.setPatientId(rs.getLong("patient_id"));
            p.setName(rs.getString("name"));
            p.setSurname(rs.getString("surname"));
            p.setSelectedDoctorId(rs.getObject("selected_doctor_id") != null ?
                    rs.getLong("selected_doctor_id") : null);
            p.setDoctorApprovalStatus(DoctorApprovalStatus.valueOf(rs.getString("doctor_approval_status")));
            return p;
        }, patientId);
    }

    /**
     * Assigns a doctor to a patient and marks the doctor approval status as PENDING.
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

        return jdbcTemplate.queryForObject(docSql, (rs, rowNum) ->
                new Doctor(
                        rs.getLong("doctor_id"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        Gender.valueOf(rs.getString("gender"))
                ), doctorId);
    }

    /**
     * Creates a new measurement session for a patient.
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
     * Saves a new Symptoms record for the given session.
     * @param sessionId The ID of the session to attach the symptoms to.
     * @param symptoms  The symptoms object to store (timestamp is auto-filled if null).
     * @return The same Symptoms instance with timestamp populated.
     * @throws IllegalStateException if the session does not belong to any patient.
     */
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

    /**
     * Saves a basic (text-based) patient signal into the database.
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

    /**
     * Retrieves all signals belonging to a session
     * @param sessionId ID of the session.
     * @return List of Signal ordered chronologically.
     */
    public List<Signal> findSignalsBySessionId(Long sessionId) {
        String sql = "SELECT signal_id, session_id, patient_id, time_stamp, patient_data, fs, signal_type FROM signals WHERE session_id = ? ORDER BY time_stamp";
        return jdbcTemplate.query(sql, new Object[]{sessionId}, (rs, rowNum) -> {
            return new Signal(
                    rs.getLong("signal_id"),
                    rs.getLong("session_id"),
                    rs.getTimestamp("time_stamp").toLocalDateTime(),
                    SignalType.valueOf(rs.getString("signal_type")),
                    rs.getString("patient_data"),
                    rs.getInt("fs")
            );
        });
    }

    /**
     * Retrieves all symptoms belonging to a session.
     *
     * @param sessionId ID of the session.
     * @return List of Symptoms.
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

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileBytes)));

        String firstLine;
        String data;
        String finalData;

        try {
            firstLine = reader.readLine();
            data = reader.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("EMG signal file is empty");
        }

        if (firstLine == null || firstLine.isBlank())
            throw new IllegalStateException("Missing sampling rate (first line).");

        if (data == null || data.isBlank())
            throw new IllegalStateException("Missing data line (second line).");

        int fs = Integer.parseInt(firstLine.trim());

        //TODO PROCESAMIENTO DE SEÑAL AQUI (de data)
        double[] rawData = SignalUtils.stringToDoubleArray(data);
        double[] signalMV = SignalUtils.convertToMV(rawData, 3.0, 10, 1000);
        //FILTER
        double[] passFilteredSignal = SignalUtils.bandpassFilter(signalMV, fs, 50, 300, 4);
        double[] filteredSignal = SignalUtils.notchFilter(passFilteredSignal, fs, 60.0, 30);
        //FOR SAVING
        finalData = SignalUtils.doubleArrayToString(filteredSignal);

        //FOR VISUALIZING
        EMGProcessor.ContractionResult contractions = EMGProcessor.detectContractions(filteredSignal, fs, 0.165, 0.10);
        EMGProcessor.plotEMGResults(fs, filteredSignal, contractions.envelope, contractions.onsets, contractions.offsets, String.valueOf(patientId));

        String sql = """
        INSERT INTO signals (patient_id, session_id, time_stamp, patient_data, fs)
        VALUES (?, ?, ?, ?, ?)
        """;

        LocalDateTime timestamp = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();


        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, patientId);
            ps.setLong(2, sessionId);
            ps.setTimestamp(3, Timestamp.valueOf(timestamp));
            ps.setString(4, finalData);
            ps.setInt(5, fs);
            return ps;
        }, keyHolder);

        Long signalId = ((Number) keyHolder.getKeys().get("signal_id")).longValue();

        return new Signal(signalId, sessionId, timestamp, SignalType.EMG, data, fs);
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

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileBytes)));

        String firstLine;
        String data;
        String finalData;
        try {
            firstLine = reader.readLine();
            data = reader.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("ECG signal file is empty");
        }

        if (firstLine == null || firstLine.isBlank())
            throw new IllegalStateException("Missing sampling rate (first line).");

        if (data == null || data.isBlank())
            throw new IllegalStateException("Missing data line (second line).");

        int fs = Integer.parseInt(firstLine.trim());

        //TODO PROCESAMIENTO DE SEÑAL AQUI (de data)
        double[] rawECG = SignalUtils.stringToDoubleArray(data);
        double[] ecgSignal = SignalUtils.convertToMV(rawECG, 3.3, 10, 1100);

        //FILTER
        double[] filteredECG = ECGProcessor.applyFilters(ecgSignal, fs);
        //FOR SAVING
        finalData = SignalUtils.doubleArrayToString(filteredECG);

        //FOR VISUALIZING
        ECGProcessor.QRSResult qrs = ECGProcessor.detectQRSComplexes(filteredECG, fs);
        double[] time = new double[filteredECG.length];
        for (int i = 0; i < time.length; i++) {
            time[i] = i / fs;
        }
        ECGProcessor.plotECGResults(time, filteredECG, qrs.qPeaks, "Análisis ECG (Simulación Servidor)", 240);
        //------------------------------------------------------------------------------------------------------------------------------

        String sql = """
        INSERT INTO signals (patient_id, session_id, time_stamp, patient_data, fs)
        VALUES (?, ?, ?, ?, ?)
        """;

        LocalDateTime timestamp = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, patientId);
            ps.setLong(2, sessionId);
            ps.setTimestamp(3, Timestamp.valueOf(timestamp));
            ps.setString(4, finalData);
            ps.setInt(5, fs);
            return ps;
        }, keyHolder);

        Long signalId = ((Number) keyHolder.getKeys().get("signal_id")).longValue();

        return new Signal(signalId, sessionId, timestamp, SignalType.ECG, data, fs);
    }



}
