package com.example.telemedicine.repository;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DoctorRepository {
    private final JdbcTemplate jdbcTemplate;

    public DoctorRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * This method gets all the patients
     * @param doctorId doctor id of the patient
     * @return list of patients
     */
    public List<Patient> findPatientsByDoctorId(Long doctorId) {
        String sql = """
            SELECT patient_id, user_id, name, surname, gender, birth_date, height, weight
            FROM patients
            WHERE doctor_id = ?
            ORDER BY name
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Patient patient = new Patient(
                    rs.getLong("patient_id"),
                    rs.getLong("user_id"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("gender") != null ? com.example.telemedicine.domain.Gender.valueOf(rs.getString("gender")) : null,
                    rs.getDate("birth_date").toLocalDate(),
                    rs.getLong("height"),
                    rs.getDouble("weight"),
                    null
            );
            return patient;
        }, doctorId);
    }


    /**
     * This method gets the measurement history (sessions) for a selected patient
     * @param patientId the patient id corresponding to the patient from who we want to see the historial as integer
     * @return the measurement session of the patient as list
     */
    public List<MeasurementSession> findPatientSessions(Long patientId) {
        String sql = """
            SELECT session_id, patient_id, time_stamp
            FROM measurement_sessions
            WHERE patient_id = ?
            ORDER BY time_stamp DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new MeasurementSession(
                        rs.getLong("session_id"),
                        rs.getLong("patient_id"),
                        rs.getTimestamp("time_stamp").toLocalDateTime(),
                        null,
                        null
                ), patientId);
    }

}
