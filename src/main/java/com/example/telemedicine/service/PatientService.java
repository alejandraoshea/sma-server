package com.example.telemedicine.service;

import com.example.telemedicine.domain.Symptoms;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.PatientRepository;

import java.util.List;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Symptoms> updateSymptoms(Long sessionId, List<Symptoms> symptoms) {
        //** assign patientId to each symptom

        for (Symptoms symptom : symptoms) {
            symptom.setMeasurementSessionId(sessionId);
        }

        patientRepository.saveSymptoms(symptoms);
        return patientRepository.findBySessionId(sessionId);
    }

    public List<Symptoms> getSymptoms(Long patientId) {
        return patientRepository.findByPatientId(patientId);
    }

}
