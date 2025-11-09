package com.example.telemedicine.controller;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.Symptoms;
import org.springframework.web.bind.annotation.*;
import com.example.telemedicine.service.MeasurementSessionService;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class MeasurementSessionController {
    private final MeasurementSessionService measurementSessionService;

    public MeasurementSessionController(MeasurementSessionService measurementSessionService) {
        this.measurementSessionService = measurementSessionService;
    }

    @PostMapping("/start/{patientId}")
    public MeasurementSession startSession(@PathVariable Long patientId) {
        return measurementSessionService.startNewSession(patientId);
    }

    @PostMapping("/{sessionId}/symptoms")
    public Symptoms addSymptoms(@PathVariable Long sessionId, @RequestBody Symptoms symptoms) {
        return measurementSessionService.uploadSymptoms(sessionId, symptoms);
    }

    @PostMapping("/{sessionId}/signals")
    public Signal addSignal(@PathVariable Long sessionId, @RequestBody Signal signal) {
        return measurementSessionService.uploadSignal(sessionId, signal);
    }

    @GetMapping("/{sessionId}/signals")
    public List<Signal> getSessionSignals(@PathVariable Long sessionId) {
        return measurementSessionService.getSignalsBySession(sessionId);
    }

    @GetMapping("/{sessionId}/symptoms")
    public List<Symptoms> getSessionSymptoms(@PathVariable Long sessionId) {
        return measurementSessionService.getSymptomsBySession(sessionId);
    }

    @GetMapping("/{patientId}/sessions")
    public List<MeasurementSession> getPatientSessions(@PathVariable Long patientId) {
        return measurementSessionService.getSessionsByPatient(patientId);
    }

    /*
    @PostMapping
    public ResponseEntity<String> uploadSignal(@RequestBody Signal signal) {
        signalService.saveSignal(signal);
        return ResponseEntity.ok("Signal saved");
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<Signal>> getPatientSignals(@PathVariable Long id) {
        return ResponseEntity.ok(signalService.getSignalsByPatient(id));
    }
     */

}
