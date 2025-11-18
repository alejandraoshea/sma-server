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

    //CRUD

}
