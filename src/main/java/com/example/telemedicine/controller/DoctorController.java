package com.example.telemedicine.controller;

import com.example.telemedicine.domain.*;

import com.example.telemedicine.repository.DoctorRepository;
import com.example.telemedicine.security.JwtService;
import com.example.telemedicine.service.PatientService;
import io.jsonwebtoken.Claims;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import com.example.telemedicine.service.PatientService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.telemedicine.service.DoctorService;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final JwtService jwtService;
    private final DoctorRepository doctorRepository;

    public DoctorController(DoctorService doctorService, PatientService patientService, JwtService jwtService, DoctorRepository doctorRepository) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.jwtService = jwtService;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Retrieves all doctors in the system
     *
     * @return list of doctors
     */
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    /**
     * Finds a doctor using the doctor ID
     *
     * @return doctor object
     */
    @GetMapping("/me")
    public ResponseEntity<?> getPatient(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer", "").trim();
        Claims claims = jwtService.extractClaims(token);

        if (!claims.get("role").equals(Role.DOCTOR.name())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        Long doctorId = claims.get("doctorId", Long.class);
        Doctor doctor = doctorRepository.findDoctorById(doctorId);

        return ResponseEntity.ok(doctor);
    }

    /**
     * Updates current doctor profile
     */
    @PostMapping("/me")
    public ResponseEntity<?> updateDoctor(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody Doctor doctorData) {
        Claims claims = extractDoctorClaims(authHeader);
        Long doctorId = claims.get("doctorId", Long.class);

        try {
            doctorService.updateDoctorInfo(doctorId, doctorData);
            Doctor updatedDoctor = doctorService.findDoctorById(doctorId);
            return ResponseEntity.ok(updatedDoctor);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to update doctor profile");
        }
    }

    /**
     * Retrieves a list of all patients assigned to a specific doctor
     *
     * @return list of patients assigned to the doctor
     */
    @GetMapping("/me/patients")
    public List<Patient> getPatients(@RequestHeader("Authorization") String authHeader) {
        Claims claims = extractDoctorClaims(authHeader);
        Long doctorId = claims.get("doctorId", Long.class);
        return doctorService.getPatientsOfDoctor(doctorId);
    }

    /**
     * Retrieves a list of patients who have requested approval to be assigned to this doctor
     *
     * @return list of pending patient requests
     */
    @GetMapping("/me/requests")
    public List<Patient> getPendingRequests(@RequestHeader("Authorization") String authHeader) {
        Claims claims = extractDoctorClaims(authHeader);
        Long doctorId = claims.get("doctorId", Long.class);
        return doctorService.getPendingRequests(doctorId);
    }

    /**
     * Approves a patient’s request to be assigned to the doctor
     *
     * @param patientId ID of the patient requesting approval
     * @return updated patient object
     */
    @PostMapping("/me/approve/{patientId}")
    public Patient approvePatient(@RequestHeader("Authorization") String authHeader,
                                  @PathVariable Long patientId) {
        Claims claims = extractDoctorClaims(authHeader);
        Long doctorId = claims.get("doctorId", Long.class);
        return doctorService.approvePatientRequest(patientId, doctorId);
    }

    /**
     * Rejects a patient’s request to be assigned to the doctor
     *
     * @param patientId ID of the patient requesting approval
     * @return updated patient object
     */
    @PostMapping("/me/reject/{patientId}")
    public Patient rejectPatient(@RequestHeader("Authorization") String authHeader,
                                 @PathVariable Long patientId) {
        Claims claims = extractDoctorClaims(authHeader);
        Long doctorId = claims.get("doctorId", Long.class);
        return doctorService.rejectPatientRequest(patientId, doctorId);
    }

    /**
     * Retrieves all measurement sessions belonging to a specific patient
     *
     * @param patientId the patient's ID
     * @return list of measurement sessions
     */
    @GetMapping("/sessions/patients/{patientId}")
    public List<MeasurementSession> getPatientSessions(@PathVariable Long patientId) {
        return patientService.getSessionsByPatient(patientId);
    }

    private Claims extractDoctorClaims(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);

        if (!"DOCTOR".equals(claims.get("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        return claims;
    }

    /**
     * Finds a doctor using the doctor ID
     *
     * @param doctorId ID of the doctor
     * @return doctor object
     */
    @GetMapping("/{doctorId}")
    public Doctor findDoctorById(@PathVariable Long doctorId) {
        return doctorService.findDoctorById(doctorId);
    }

    /**
     * Retrieves a list of patients who have requested approval to be assigned to this doctor
     *
     * @param doctorId ID of the doctor
     * @return list of pending patient requests
     */
    @GetMapping("/{doctorId}/requests")
    public List<Patient> getPendingRequests(@PathVariable Long doctorId) {
        return doctorService.getPendingRequests(doctorId);
    }

    /**
     * Approves a patient’s request to be assigned to the doctor
     *
     * @param doctorId  ID of the doctor
     * @param patientId ID of the patient requesting approval
     * @return updated patient object
     */
    @PostMapping("/{doctorId}/approve/{patientId}")
    public Patient acceptPatientRequest(@PathVariable Long doctorId, @PathVariable Long patientId) {
        return doctorService.approvePatientRequest(patientId, doctorId);
    }

    /**
     * Rejects a patient’s request to be assigned to the doctor
     *
     * @param doctorId  ID of the doctor
     * @param patientId ID of the patient requesting approval
     * @return updated patient object
     */
    @PostMapping("/{doctorId}/reject/{patientId}")
    public Patient rejectPatientRequest(@PathVariable Long doctorId, @PathVariable Long patientId) {
        return doctorService.rejectPatientRequest(patientId, doctorId);
    }

    @PostMapping("/{doctorId}/report/{sessionId}/generate")
    public Report generateReport(
            @PathVariable Long doctorId,
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Claims claims = jwtService.extractClaims(authHeader.replace("Bearer ", ""));
        Long tokenDoctorId = claims.get("doctorId", Long.class);

        if (!doctorId.equals(tokenDoctorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot generate reports for other doctors");
        }

        return doctorService.generateReport(doctorId, sessionId);
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<byte[]> getReport(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reportId) {

        try {
            String token = authHeader.replace("Bearer ", "").trim();
            Claims claims = jwtService.extractClaims(token);
            Long doctorId = claims.get("doctorId", Long.class);

            Report report = doctorService.getReport(reportId, doctorId);

            if (report == null) {
                return ResponseEntity.status(403).build();
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + report.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(report.getFileData());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/localities")
    public List<Locality> getAllLocalities() {
        return doctorService.getAllLocalities();
    }

    @PostMapping("/localities")
    public Locality addLocality(@RequestBody Locality locality) {
        return doctorService.addLocality(locality);
    }


}
