package com.example.telemedicine.service;

import com.example.telemedicine.domain.Doctor;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.PatientRepository;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    //CRUD operations

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


}
