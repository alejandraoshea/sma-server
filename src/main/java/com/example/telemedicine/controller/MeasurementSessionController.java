package com.example.telemedicine.controller;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.SymptomType;
import com.example.telemedicine.domain.Symptoms;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.telemedicine.service.MeasurementSessionService;

import java.io.IOException;
import java.util.Arrays;
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

    @GetMapping("/enum")
    public List<String> getSymptomEnum() {
        return Arrays.stream(SymptomType.values())
                .map(Enum::name)
                .toList();
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

    //al parecer post mapping es mandar datos al servidor
    //si hay varios patients no diferencia entre patients potque no tienen el patient ID,
    // habría que mandar el patient ID al client cuando empiece la conxión para poder mandar información
    // diferenciada al server no?????
    @PostMapping(value="/{sessionId}/{patientId}/ecg", consumes=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Signal receiveECG(@PathVariable("sessionId") Long sessionId, @RequestBody byte[] fileBytes) {
        return measurementSessionService.addECG(fileBytes, sessionId);
    }

    @PostMapping(value="/{sessionId}/{patientId}/emg", consumes=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Signal receiveEMG(@PathVariable("sessionId") Long sessionId, @RequestBody byte[] fileBytes) throws IOException {
        return measurementSessionService.addEMG(fileBytes, sessionId);
    }

}
