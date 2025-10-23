package com.example.telemedicine.service;

import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.PatientRepository;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }


    //** recordSymptom or saveSymptom, getSymptomsForPatient,..?
    /*public void recordSymptom(Symptom symptom) {
        symptomRepository.save(symptom);
    }

    public List<Symptom> getSymptomsForPatient(Long patientId) {
        return symptomRepository.findByPatientId(patientId);
    }
     */
}
