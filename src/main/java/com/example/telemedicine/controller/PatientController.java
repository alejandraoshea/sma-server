package com.example.telemedicine.controller;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.repository.PatientRepository;
import com.example.telemedicine.security.JwtService;
import com.example.telemedicine.service.DoctorService;
import com.example.telemedicine.service.PatientService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final JwtService jwtService;
    private final PatientRepository patientRepository;

    public PatientController(PatientService patientService, DoctorService doctorService, JwtService jwtService,
                             PatientRepository patientRepository) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.jwtService = jwtService;
        this.patientRepository = patientRepository;
    }

    //** CRUD and see all sessions

    /**
     * Submits a request for a patient to connect with a doctor
     *
     * @param doctorId ID of doctor
     * @return doctor object confirming submission
     */
    @PostMapping("/request/{doctorId}")
    public Doctor selectDoctorFromList(@PathVariable Long doctorId,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);
        Long patientId = claims.get("patientId", Long.class);
        return patientService.selectDoctorFromList(patientId, doctorId);
    }

    /**
     * Retrieves patient information
     *
     * @return patient object
     */
    @GetMapping("/me")
    public ResponseEntity<?> getPatient(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);

        String role = claims.get("role", String.class);
        if (!role.equals(Role.PATIENT.name()) && !role.equals(Role.DOCTOR.name())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        Long patientId = claims.get("patientId", Long.class);
        Patient patient = patientRepository.findById(patientId);

        return ResponseEntity.ok(patient);
    }

    /**
     * Updates the patient's profile information
     *
     * @param patientData Patient object containing updated fields
     * @return updated Patient
     */
    @PostMapping("/me")
    public ResponseEntity<?> updatePatient(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Patient patientData
    ) {
        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);

        String role = claims.get("role", String.class);
        if (!role.equals(Role.PATIENT.name()) && !role.equals(Role.DOCTOR.name())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        Long patientId = claims.get("patientId", Long.class);
        System.out.println("PatientId: " + patientId);
        System.out.println("PatientData: " + patientData);

        try {
            patientService.updatePatientInfo(patientId, patientData);

            Patient updatedPatient = patientService.findById(patientId);

            return ResponseEntity.ok(updatedPatient);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to update patient");
        }
    }

    /**
     * Starts a new measurement session
     *
     * @return created measurement session
     */
    @PostMapping("/sessions/start/me")
    public MeasurementSession startSession(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);
        Long patientId = claims.get("patientId", Long.class);
        return patientService.startNewSession(patientId);
    }

    /**
     * Uploads symptoms for a given session
     *
     * @param sessionId session ID
     * @param symptoms  symptoms data
     * @return saved symptoms
     */
    @PostMapping("/sessions/{sessionId}/symptoms")
    public Set<SymptomType> addSymptoms(@PathVariable Long sessionId, @RequestBody Set<SymptomType> symptoms) {
        return patientService.uploadSymptoms(sessionId, symptoms);
    }

    /**
     * Retrieves a list of all possible symptom enum values
     *
     * @return list of symptom names
     */
    @GetMapping("/sessions/enum")
    public List<String> getSymptomEnum() {
        return Arrays.stream(SymptomType.values())
                .map(Enum::name)
                .toList();
    }


    /**
     * Uploads a signal for a session
     *
     * @param sessionId session ID
     * @param signal    signal data
     * @return saved signal object
     */
    @PostMapping("/sessions/{sessionId}/signals")
    public Signal addSignal(@PathVariable Long sessionId, @RequestBody Signal signal) {
        return patientService.uploadSignal(sessionId, signal);
    }

    /**
     * Retrieves all signals of a session
     *
     * @param sessionId session ID
     * @return list of signals
     */
    @GetMapping("/sessions/{sessionId}/signals")
    public List<Signal> getSessionSignals(@PathVariable Long sessionId) {
        return patientService.getSignalsBySession(sessionId);
    }

    /**
     * Retrieves all symptoms of a session
     *
     * @param sessionId session ID
     * @return list of symptoms
     */
    @GetMapping("/sessions/{sessionId}/symptoms")
    public Set<SymptomType> getSessionSymptoms(@PathVariable Long sessionId) {
        return patientService.getSymptomsBySession(sessionId);
    }

    /**
     * Retrieves all sessions for a patient
     *
     * @return list of sessions
     */
    @GetMapping("/sessions/{patientId}")
    public ResponseEntity<?> getPatientSessions(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long patientId
    ) {
        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);
        String role = claims.get("role", String.class);

        if (Role.PATIENT.name().equals(role)) {
            Long jwtPatientId = claims.get("patientId", Long.class);
            if (!jwtPatientId.equals(patientId)) {
                return ResponseEntity.status(403).body("Forbidden: cannot access other patients' sessions");
            }
        } else if (!Role.DOCTOR.name().equals(role)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        List<MeasurementSession> sessions = patientService.getSessionsByPatient(patientId);
        return ResponseEntity.ok(sessions);
    }

    //al parecer post mapping es mandar datos al servidor
    //si hay varios patients no diferencia entre patients potque no tienen el patient ID,
    // habría que mandar el patient ID al client cuando empiece la conxión para poder mandar información
    // diferenciada al server no?????

    /**
     * Handles ECG file upload (binary format)
     *
     * @param sessionId session ID
     * @param fileBytes file content
     * @return stored ECG signal
     */
    @PostMapping(value = "/sessions/{sessionId}/ecg", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Signal receiveECG(@PathVariable("sessionId") Long sessionId, @RequestBody byte[] fileBytes) {
        return patientService.addECG(fileBytes, sessionId);
    }

    /**
     * Handles EMG file upload (binary format)
     *
     * @param sessionId session ID
     * @param fileBytes raw EMG file
     * @return stored EMG signal
     * @throws IOException if reading fails
     */
    @PostMapping(value = "/sessions/{sessionId}/emg", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Signal receiveEMG(@PathVariable("sessionId") Long sessionId, @RequestBody byte[] fileBytes) throws IOException {
        return patientService.addEMG(fileBytes, sessionId);
    }

    /**
     * Generates a CSV summary file for a given session and saves it.
     *
     * @param sessionId the ID of the session for which to generate the summary
     * @return a ResponseEntity containing a success message if generation succeeds,
     * a 404 status if the session is not found, or a 500 status for unexpected errors
     */
    @PostMapping("/sessions/{sessionId}/generate-session-file")
    public ResponseEntity<String> generateSummary(@PathVariable Long sessionId) {
        try {
            patientService.generateAndSaveCsvSummary(sessionId);
            return ResponseEntity.ok("CSV summary generated and saved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Downloads the CSV summary file for a given session.
     *
     * @param sessionId the ID of the session whose CSV file is to be downloaded
     * @return a ResponseEntity containing the CSV file as a byte array with proper headers,
     * or a 404 status if no file exists
     */
    @GetMapping("/sessions/{sessionId}/session-file")
    public ResponseEntity<byte[]> downloadSummaryFile(@PathVariable Long sessionId) {
        byte[] csvBytes = patientService.getCsvSummaryFile(sessionId);
        if (csvBytes == null || csvBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"session_" + sessionId + "_file.csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csvBytes);
    }

    /**
     * Uploads an EMG signal file for a specific session.
     *
     * @param sessionId the ID of the session to which the EMG data belongs
     * @param file      the uploaded EMG file as a multipart/form-data
     * @return the Signal object representing the uploaded EMG data
     * @throws IOException if reading the uploaded file fails
     */
    @PostMapping(value = "/sessions/{sessionId}/emg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Signal uploadEMG(@PathVariable Long sessionId,
                            @RequestParam("file") MultipartFile file) throws IOException {

        return patientService.addEMG(file.getBytes(), sessionId);
    }

    /**
     * Uploads an ECG signal file for a specific session.
     *
     * @param sessionId the ID of the session to which the ECG data belongs
     * @param file      the uploaded ECG file as a multipart/form-data
     * @return the Signal object representing the uploaded ECG data
     * @throws IOException if reading the uploaded file fails
     */
    @PostMapping(value = "/sessions/{sessionId}/ecg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Signal uploadECG(@PathVariable Long sessionId,
                            @RequestParam("file") MultipartFile file) throws IOException {

        return patientService.addECG(file.getBytes(), sessionId);
    }

    /**
     * Retrieves a list of doctors for mapping purposes for the currently authenticated patient.
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return a list of Doctor objects containing location information for mapping
     */
    @GetMapping("/me/map-doctors")
    public List<Doctor> getDoctorsForMap(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        Claims claims = jwtService.extractClaims(token);
        Long patientId = claims.get("patientId", Long.class);
        return patientService.getDoctorsForMap(patientId);
    }

    /**
     * Retrieves the assigned doctor for the currently authenticated patient.
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return a ResponseEntity containing the Doctor object if assigned,
     * a 204 No Content if no doctor is assigned,
     * or a 500 Internal Server Error in case of unexpected failures
     */
    @GetMapping("/me/doctor")
    public ResponseEntity<Doctor> getDoctor(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();

            Claims claims = jwtService.extractClaims(token);
            Long patientId = claims.get("patientId", Long.class);

            Patient patient = patientService.findById(patientId);

            if (patient.getSelectedDoctorId() == null) {
                return ResponseEntity.noContent().build();
            }

            Doctor doctor = doctorService.findDoctorById(patient.getSelectedDoctorId());

            return ResponseEntity.ok(doctor);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Retrieves all medical reports for the currently authenticated patient.
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return a ResponseEntity containing a list of Report objects,
     * a 404 status if the patient does not exist,
     * or a 500 status in case of unexpected errors
     */
    @GetMapping("/me/reports")
    public ResponseEntity<List<Report>> getReports(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            Claims claims = jwtService.extractClaims(token);
            Long patientId = claims.get("patientId", Long.class);

            Patient patient = patientService.findById(patientId);
            if (patient == null) {
                return ResponseEntity.status(404).build();
            }

            List<Report> reports = patientService.findReportsByPatientId(patientId);

            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Downloads a specific report for the currently authenticated patient.
     *
     * @param reportId   the ID of the report to download
     * @param authHeader the Authorization header containing the Bearer token
     * @return a ResponseEntity containing the report file as a byte array with proper headers,
     * or a 404 status if the report does not exist or does not belong to the patient
     */
    @GetMapping("/me/reports/{reportId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long reportId,
                                                 @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            Claims claims = jwtService.extractClaims(token);
            Long patientId = claims.get("patientId", Long.class);

            Report report = patientService.findReportByReportId(reportId);
            if (report == null || !report.getPatientId().equals(patientId)) {
                return ResponseEntity.notFound().build();
            }

            byte[] reportBytes = report.getFileData();
            if (reportBytes == null || reportBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }

            String fileName = report.getFileName() != null ? report.getFileName() : "report_" + reportId + ".pdf";
            MediaType mediaType = report.getFileType() != null && report.getFileType().equalsIgnoreCase("text/csv")
                    ? MediaType.TEXT_PLAIN
                    : MediaType.APPLICATION_PDF;

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(mediaType)
                    .body(reportBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
