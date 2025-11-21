package com.example.telemedicine.service;

import com.example.telemedicine.domain.DoctorApprovalStatus;
import com.example.telemedicine.domain.Patient;
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

    /*
    public Patient selectDoctorFromList(Long patientId, Long doctorId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        patient.setSelectedDoctorId(doctorId);
        patient.setDoctorApprovalStatus(DoctorApprovalStatus.PENDING);

        return patientRepository.save(patient);
    }

    public Patient approvePatientRequest(Long patientId, Long doctorId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!doctorId.equals(patient.getSelectedDoctorId()))
            throw new RuntimeException("This doctor was not selected by the patient");

        patient.setDoctorApprovalStatus(DoctorApprovalStatus.APPROVED);
        return patientRepository.save(patient);
    }

    public Patient rejectPatientRequest(Long patientId, Long doctorId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!doctorId.equals(patient.getSelectedDoctorId()))
            throw new RuntimeException("This doctor was not selected by the patient");

        patient.setDoctorApprovalStatus(DoctorApprovalStatus.REJECTED);
        return patientRepository.save(patient);
    }

     */
}
