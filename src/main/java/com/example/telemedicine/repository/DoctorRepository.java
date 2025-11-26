package com.example.telemedicine.repository;

import com.example.telemedicine.domain.Gender;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.domain.Doctor;
import com.example.telemedicine.domain.Report;
import com.example.telemedicine.repository.mapper.PatientRowMapper;
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
                           height, weight, sessions, doctor_id,
                           selected_doctor_id, doctor_approval_status
                    FROM patients
                    WHERE doctor_id = ?
                    ORDER BY name
                """;

        return jdbcTemplate.query(sql, new PatientRowMapper(), doctorId);
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
                    SELECT doctor_id, name, surname, gender
                    FROM doctors
                    WHERE doctor_id = ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new Doctor(
                        rs.getLong("doctor_id"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        Gender.valueOf(rs.getString("gender"))
                ), doctorId);
    }

    /**
     * This method saves a report as a pdf in the database
     * @param report the report to be saved
     * @return the report saved
     */
    public Report saveReport(Report report) {
        String sql = """
            INSERT INTO reports (patient_id, doctor_id, session_id, file_name, file_type, file_data)
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
     * This method retrieves the pdf (report) by the report id
     * @param reportId the id of the report to retrieve
     * @return the report
     */
    public Report findReportById(Long reportId) {
        String sql = "SELECT * FROM reports WHERE report_id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Report r = new Report(rs.getLong("report_id"),
                    rs.getLong("patient_id"),
                    rs.getLong("doctor_id"),
                    rs.getLong("session_id"),
                    rs.getBytes("file_data"),
                    rs.getString("file_name"),
                    rs.getString("file_type"));
            return r;
        }, reportId);
    }

}
