package com.example.telemedicine.service;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.PatientRepository;

import java.io.IOException;
import java.util.List;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public Doctor selectDoctorFromList(Long patientId, Long doctorId) {
        return patientRepository.sendDoctorRequest(patientId, doctorId);
    }

    public Patient findById(Long patientId) {
        Patient p = patientRepository.findById(patientId);

        if (p.getSelectedDoctorId() != null) {
            Doctor d = doctorRepository.findDoctorById(p.getSelectedDoctorId());
            p.setSelectedDoctorId(d.getDoctorId());
        }
        return p;
    }

    public MeasurementSession startNewSession(Long patientId) {
        return patientRepository.startNewSession(patientId);
    }

    public Signal uploadSignal(Long sessionId, Signal signal) {
        return patientRepository.saveSignal(sessionId, signal);
    }

    public Symptoms uploadSymptoms(Long sessionId, Symptoms symptoms) {
        return patientRepository.saveSymptoms(sessionId, symptoms);
    }

    public List<Signal> getSignalsBySession(Long sessionId) {
        return patientRepository.findSignalsBySessionId(sessionId);
    }

    public List<Symptoms> getSymptomsBySession(Long sessionId) {
        return patientRepository.findSymptomsBySessionId(sessionId);
    }

    public List<MeasurementSession> getSessionsByPatient(Long patientId) {
        return patientRepository.findSessionsByPatientId(patientId);
    }

    public Signal addEMG(byte[] signal, Long sessionId) throws IOException {
        return patientRepository.addEMG(signal, sessionId);
    }

    public Signal addECG(byte[] signal, Long sessionId) {
        return patientRepository.addECG(signal, sessionId);
    }

}
