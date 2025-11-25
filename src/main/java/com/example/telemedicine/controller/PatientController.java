package com.example.telemedicine.controller;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.service.PatientService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    //** CRUD and see all sessions

    @PostMapping("/{patientId}/request/{doctorId}")
    public Doctor selectDoctorFromList(@PathVariable Long patientId, @PathVariable Long doctorId) {
        return patientService.selectDoctorFromList(patientId, doctorId);
    }

    @GetMapping("/{patientId}")
    public Patient getPatient(@PathVariable Long patientId) {
        return patientService.findById(patientId);
    }

    //**Session related endpoints
    @PostMapping("/sessions/start/{patientId}")
    public MeasurementSession startSession(@PathVariable Long patientId) {
        return patientService.startNewSession(patientId);
    }

    @PostMapping("/sessions/{sessionId}/symptoms")
    public Symptoms addSymptoms(@PathVariable Long sessionId, @RequestBody Symptoms symptoms) {
        return patientService.uploadSymptoms(sessionId, symptoms);
    }

    @GetMapping("/sessions/enum")
    public List<String> getSymptomEnum() {
        return Arrays.stream(SymptomType.values())
                .map(Enum::name)
                .toList();
    }

    @PostMapping("/sessions/{sessionId}/signals")
    public Signal addSignal(@PathVariable Long sessionId, @RequestBody Signal signal) {
        return patientService.uploadSignal(sessionId, signal);
    }

    @GetMapping("/sessions/{sessionId}/signals")
    public List<Signal> getSessionSignals(@PathVariable Long sessionId) {
        return patientService.getSignalsBySession(sessionId);
    }

    @GetMapping("/sessions/{sessionId}/symptoms")
    public List<Symptoms> getSessionSymptoms(@PathVariable Long sessionId) {
        return patientService.getSymptomsBySession(sessionId);
    }

    @GetMapping("/sessions/{patientId}/sessions")
    public List<MeasurementSession> getPatientSessions(@PathVariable Long patientId) {
        return patientService.getSessionsByPatient(patientId);
    }

    //al parecer post mapping es mandar datos al servidor
    //si hay varios patients no diferencia entre patients potque no tienen el patient ID,
    // habría que mandar el patient ID al client cuando empiece la conxión para poder mandar información
    // diferenciada al server no?????
    @PostMapping(value="/sessions/{sessionId}/{patientId}/ecg", consumes= MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Signal receiveECG(@PathVariable("sessionId") Long sessionId, @RequestBody byte[] fileBytes) {
        return patientService.addECG(fileBytes, sessionId);
    }

    @PostMapping(value="/sessions/{sessionId}/{patientId}/emg", consumes=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Signal receiveEMG(@PathVariable("sessionId") Long sessionId, @RequestBody byte[] fileBytes) throws IOException {
        return patientService.addEMG(fileBytes, sessionId);
    }
}
