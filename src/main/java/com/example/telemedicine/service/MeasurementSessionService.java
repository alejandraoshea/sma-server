package com.example.telemedicine.service;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.Symptoms;
import com.example.telemedicine.repository.PatientRepository;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.MeasurementSessionRepository;

import java.io.IOException;
import java.util.List;


@Service
public class   MeasurementSessionService {
    private final MeasurementSessionRepository measurementSessionRepository;

    public MeasurementSessionService(MeasurementSessionRepository measurementSessionRepository) {
        this.measurementSessionRepository = measurementSessionRepository;
    }

    public MeasurementSession startNewSession(Long patientId) {
        return measurementSessionRepository.startNewSession(patientId);
    }

    public Signal uploadSignal(Long sessionId, Signal signal) {
        return measurementSessionRepository.saveSignal(sessionId, signal);
    }

    public Symptoms uploadSymptoms(Long sessionId, Symptoms symptoms) {
        return measurementSessionRepository.saveSymptoms(sessionId, symptoms);
    }

    public List<Signal> getSignalsBySession(Long sessionId) {
        return measurementSessionRepository.findSignalsBySessionId(sessionId);
    }

    public List<Symptoms> getSymptomsBySession(Long sessionId) {
        return measurementSessionRepository.findSymptomsBySessionId(sessionId);
    }

    public List<MeasurementSession> getSessionsByPatient(Long patientId) {
        return measurementSessionRepository.findSessionsByPatientId(patientId);
    }

    public Signal addEMG(byte[] signal, Long sessionId) throws IOException {
        return measurementSessionRepository.addEMG(signal, sessionId);
    }

    public Signal addECG(byte[] signal, Long sessionId) {
        return measurementSessionRepository.addECG(signal, sessionId);
    }


}
