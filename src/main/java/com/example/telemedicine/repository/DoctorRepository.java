package com.example.telemedicine.repository;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.repository.mapper.PatientRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class DoctorRepository {
    private final JdbcTemplate jdbcTemplate;

    public DoctorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Finds the doctor ID associated with a given user ID.
     *
     * @param userId ID of the user
     * @return The corresponding doctor ID, or null if not found
     */
    public Long findDoctorIdByUserId(Long userId) {
        try {
            String sql = "SELECT doctor_id FROM doctors WHERE user_id = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, userId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Find the patients of a doctor using the doctor ID
     *
     * @param doctorId the ID of the doctor
     * @return a list of the doctor's patients
     */
    public List<Patient> findPatientsByDoctorId(Long doctorId) {
        String sql = """
                    SELECT patient_id, user_id, name, surname, gender, birth_date,
                           height, weight, doctor_id,
                           selected_doctor_id, doctor_approval_status
                    FROM patients
                    WHERE doctor_id = ?
                    ORDER BY name
                """;

        return jdbcTemplate.query(sql, new PatientRowMapper(), doctorId);
    }

    /**
     * Updates doctor's personal information in the database using SQL.
     *
     * @param doctorId ID of the patient to update
     * @param newData  Doctor object containing new data (null fields are ignored)
     * @return number of rows updated (should be 1 if successful)
     */
    public int updateDoctorInfo(Long doctorId, Doctor newData) {
        Long localityId = null;
        if (newData.getLocality() != null && newData.getLocality().getName() != null) {
            try {
                localityId = jdbcTemplate.queryForObject(
                        "SELECT locality_id FROM localities WHERE name = ?",
                        Long.class,
                        newData.getLocality().getName()
                );
            } catch (EmptyResultDataAccessException e) {
                e.printStackTrace();
            }
        }

        String sql = """
                    UPDATE doctors
                    SET
                        name = COALESCE(?, name),
                        surname = COALESCE(?, surname),
                        gender = COALESCE(?::gender_enum, gender),
                        locality_id = COALESCE(?, locality_id)
                    WHERE doctor_id = ?
                """;

        return jdbcTemplate.update(sql,
                newData.getName(),
                newData.getSurname(),
                newData.getGender() != null ? newData.getGender().name() : null,
                localityId,
                doctorId
        );
    }

    /**
     * Get list of all doctors
     *
     * @return a list of all the doctors in the BBDD
     */
    public List<Doctor> getAllDoctors() {
        String sql = """
                    SELECT doctor_id, user_id, name, surname, gender
                    FROM doctors
                    ORDER BY name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Doctor(
                        rs.getLong("doctor_id"),
                        rs.getLong("user_id"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("gender") != null ? Gender.valueOf(rs.getString("gender")) : null
                )
        );
    }

    /**
     * Get pending patient requests for doctor approval
     *
     * @param doctorId the id of the requested doctor
     * @return a list of the patient's that requested that doctor
     */
    public List<Patient> getPendingRequests(Long doctorId) {
        String sql = """
                SELECT *
                FROM patients
                WHERE selected_doctor_id = ?
                AND doctor_approval_status = 'PENDING'
                """;

        return jdbcTemplate.query(sql, new PatientRowMapper(), doctorId);
    }


    /**
     * Approve a patient's doctor request
     *
     * @param patientId the id of the patient who requested a doctor
     * @param doctorId  the id of the doctor requested
     * @return the patient approved by the doctor
     */
    public Patient approvePatientRequest(Long patientId, Long doctorId) {
        String sql = """
                UPDATE patients
                SET doctor_approval_status = 'APPROVED',
                    doctor_id = selected_doctor_id
                WHERE patient_id = ? AND selected_doctor_id = ?
                """;

        jdbcTemplate.update(sql, patientId, doctorId);

        return getPatient(patientId);
    }

    /**
     * Reject a patient's doctor request
     *
     * @param patientId the id of the patient rejected
     * @param doctorId  the id of the doctor requested
     * @return the patient rejected
     */
    public Patient rejectPatientRequest(Long patientId, Long doctorId) {
        String sql = """
                UPDATE patients
                SET doctor_approval_status = 'REJECTED'
                WHERE patient_id = ? AND selected_doctor_id = ?
                """;

        jdbcTemplate.update(sql, patientId, doctorId);

        return getPatient(patientId);
    }

    /**
     * Get patients approved for a doctor
     *
     * @param doctorId the id of the doctor to see all the patients approved by him
     * @return the list of patients assigned to that doctor
     */
    public List<Patient> getApprovedPatients(Long doctorId) {
        String sql = """
                SELECT *
                FROM patients
                WHERE selected_doctor_id = ?
                AND doctor_approval_status = 'APPROVED'
                """;

        return jdbcTemplate.query(sql, new PatientRowMapper(), doctorId);
    }


    /**
     * Get a single patient by ID
     *
     * @param patientId the id of the patient
     * @return the patient as a Patient
     */
    private Patient getPatient(Long patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        return jdbcTemplate.queryForObject(sql, new PatientRowMapper(), patientId);
    }

    public Doctor findDoctorById(Long doctorId) {
        String sql = """
                 SELECT d.doctor_id, d.name, d.surname, d.gender,
                        l.locality_id, l.name AS localityName, l.latitude, l.longitude
                 FROM doctors d
                 LEFT JOIN localities l ON d.locality_id = l.locality_id
                 WHERE d.doctor_id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Gender gender = null;
                String genderStr = rs.getString("gender");
                if (genderStr != null) gender = Gender.valueOf(genderStr);

                Locality locality = null;
                if (rs.getObject("locality_id") != null) {
                    locality = new Locality(
                            rs.getLong("locality_id"),
                            rs.getString("localityName"),
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude")
                    );
                }

                return new Doctor(
                        rs.getLong("doctor_id"), rs.getString("name"),
                        rs.getString("surname"), gender, locality
                );
            }, doctorId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * This method saves a report as a pdf in the database
     *
     * @param report the report to be saved
     * @return the report saved
     */
    public Report saveReport(Report report) {
        String sql = """
                    INSERT INTO report (patient_id, doctor_id, session_id, file_name, file_type, file_data)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, report.getPatientId());
            ps.setLong(2, report.getDoctorId());
            ps.setLong(3, report.getSessionId());
            ps.setString(4, report.getFileName());
            ps.setString(5, report.getFileType());
            ps.setBytes(6, report.getFileData());
            return ps;
        }, keyHolder);

        Long id = ((Number) keyHolder.getKeys().get("report_id")).longValue();
        report.setReportId(id);
        return report;
    }

    /**
     * This method retrieves the PDF (report) by the report id and verifies the doctor owns it
     *
     * @param reportId the id of the report to retrieve
     * @param doctorId the id of the doctor requesting the report
     * @return the report if it belongs to the doctor, otherwise null
     */
    public Report findReportById(Long reportId, Long doctorId) {
        String sql = "SELECT * FROM report WHERE report_id = ? AND doctor_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                return new Report(
                        rs.getLong("report_id"),
                        rs.getLong("patient_id"),
                        rs.getLong("doctor_id"),
                        rs.getLong("session_id"),
                        rs.getBytes("file_data"),
                        rs.getString("file_name"),
                        rs.getString("file_type"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }, reportId, doctorId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Locality> getAllLocalities() {
        String sql = "SELECT locality_id, name, latitude, longitude FROM localities ORDER BY name";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Locality(
                        rs.getLong("locality_id"),
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                )
        );
    }

    public Locality findLocalityById(Long id) {
        String sql = "SELECT locality_id, name, latitude, longitude FROM localities WHERE locality_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new Locality(
                        rs.getLong("locality_id"),
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                ), id);
    }

    public List<Report> getAllReports(Long doctorId) {
        String sql = "SELECT * FROM report WHERE doctor_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Report(
                rs.getLong("report_id"),
                rs.getLong("patient_id"),
                rs.getLong("doctor_id"),
                rs.getLong("session_id"),
                rs.getBytes("file_data"),
                rs.getString("file_name"),
                rs.getString("file_type"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ), doctorId);
    }
}
