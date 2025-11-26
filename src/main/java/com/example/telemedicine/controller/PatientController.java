package com.example.telemedicine.controller;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.repository.PatientRepository;
import com.example.telemedicine.repository.UserRepository;
import com.example.telemedicine.security.JwtService;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public PatientController(PatientService patientService, JwtService jwtService, UserRepository userRepository,
                             PatientRepository patientRepository) {
        this.patientService = patientService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
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

        if (!claims.get("role").equals(Role.PATIENT.name())) {
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

        if (!claims.get("role").equals(Role.PATIENT.name())) {
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

    //**Session related endpoints

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
    @GetMapping("/sessions/me")
    public List<MeasurementSession> getPatientSessions(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);
        Long patientId = claims.get("patientId", Long.class);
        return patientService.getSessionsByPatient(patientId);
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
}
