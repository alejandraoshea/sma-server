package com.example.telemedicine.service;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.Symptoms;
import com.example.telemedicine.repository.PatientRepository;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.MeasurementSessionRepository;

import java.util.List;


@Service
public class MeasurementSessionService {
    private final MeasurementSessionRepository measurementSessionRepository;
    private final PatientRepository patientRepository;

    public MeasurementSessionService(MeasurementSessionRepository measurementSessionRepository, PatientRepository patientRepository) {
        this.measurementSessionRepository = measurementSessionRepository;
        this.patientRepository = patientRepository;
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
        return patientRepository.findBySessionId(sessionId);
    }

    public List<MeasurementSession> getSessionsByPatient(Long patientId) {
        return measurementSessionRepository.findSessionsByPatientId(patientId);
    }

    //** we could do:
    /*
    public void saveSignal(Signal signal) {
        signalRepository.save(signal);
    }

    public List<Signal> getSignalsByPatient(Long patientId) {
        return signalRepository.findByPatientId(patientId);
    }
     */

}
