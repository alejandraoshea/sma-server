package com.example.telemedicine.controller;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.service.PatientService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    //** CRUD and see all sessions

    /**
     * Submits a request for a patient to connect with a doctor
     * @param patientId ID of patient
     * @param doctorId ID of doctor
     * @return doctor object confirming submission
     */
    @PostMapping("/{patientId}/request/{doctorId}")
    public Doctor selectDoctorFromList(@PathVariable Long patientId, @PathVariable Long doctorId) {
        return patientService.selectDoctorFromList(patientId, doctorId);
    }

    /**
     * Retrieves patient information
     * @param patientId ID of the patient
     * @return patient object
     */
    @GetMapping("/{patientId}")
    public Patient getPatient(@PathVariable Long patientId) {
        return patientService.findById(patientId);
    }

    //**Session related endpoints

    /**
     * Starts a new measurement session
     * @param patientId ID of patient starting the session
     * @return created measurement session
     */
    @PostMapping("/sessions/start/{patientId}")
    public MeasurementSession startSession(@PathVariable Long patientId) {
        return patientService.startNewSession(patientId);
    }

    /**
     * Uploads symptoms for a given session
     * @param sessionId session ID
     * @param symptoms symptoms data
     * @return saved symptoms
     */
    @PostMapping("/sessions/{sessionId}/symptoms")
    public Set<SymptomType> addSymptoms(@PathVariable Long sessionId, @RequestBody Set<SymptomType> symptoms) {
        return patientService.uploadSymptoms(sessionId, symptoms);
    }

    /**
     * Retrieves a list of all possible symptom enum values
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
     * @param sessionId session ID
     * @param signal signal data
     * @return saved signal object
     */
    @PostMapping("/sessions/{sessionId}/signals")
    public Signal addSignal(@PathVariable Long sessionId, @RequestBody Signal signal) {
        return patientService.uploadSignal(sessionId, signal);
    }

    /**
     * Retrieves all signals of a session
     * @param sessionId session ID
     * @return list of signals
     */
    @GetMapping("/sessions/{sessionId}/signals")
    public List<Signal> getSessionSignals(@PathVariable Long sessionId) {
        return patientService.getSignalsBySession(sessionId);
    }

    /**
     * Retrieves all symptoms of a session
     * @param sessionId session ID
     * @return list of symptoms
     */
    @GetMapping("/sessions/{sessionId}/symptoms")
    public Set<SymptomType> getSessionSymptoms(@PathVariable Long sessionId) {
        return patientService.getSymptomsBySession(sessionId);
    }

    /**
     * Retrieves all sessions for a patient
     * @param patientId ID of the patient
     * @return list of sessions
     */
    @GetMapping("/sessions/{patientId}")
    public List<MeasurementSession> getPatientSessions(@PathVariable Long patientId) {
        return patientService.getSessionsByPatient(patientId);
    }

    //al parecer post mapping es mandar datos al servidor
    //si hay varios patients no diferencia entre patients potque no tienen el patient ID,
    // habría que mandar el patient ID al client cuando empiece la conxión para poder mandar información
    // diferenciada al server no?????
    /**
     * Handles ECG file upload (binary format)
     * @param sessionId session ID
     * @param fileBytes file content
     * @return stored ECG signal
     */
    @PostMapping(value="/sessions/{sessionId}/{patientId}/ecg", consumes= MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Signal receiveECG(@PathVariable("sessionId") Long sessionId, @RequestBody byte[] fileBytes) {
        return patientService.addECG(fileBytes, sessionId);
    }


    /**
     * Handles EMG file upload (binary format)
     * @param sessionId session ID
     * @param fileBytes raw EMG file
     * @return stored EMG signal
     * @throws IOException if reading fails
     */
    @PostMapping(value="/sessions/{sessionId}/{patientId}/emg", consumes=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Signal receiveEMG(@PathVariable("sessionId") Long sessionId, @RequestBody byte[] fileBytes) throws IOException {
        return patientService.addEMG(fileBytes, sessionId);
    }
}
