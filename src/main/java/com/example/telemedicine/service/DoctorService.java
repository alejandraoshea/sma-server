package com.example.telemedicine.service;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.DoctorRepository;

import java.util.List;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Patient> getDoctorPatients(Long doctorId) {
        return doctorRepository.findPatientsByDoctorId(doctorId);
    }

    public List<MeasurementSession> getPatientSessions(Long patientId) {
        return doctorRepository.findPatientSessions(patientId);
    }

}
