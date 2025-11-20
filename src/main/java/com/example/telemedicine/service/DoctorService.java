package com.example.telemedicine.service;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.repository.MeasurementSessionRepository;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.DoctorRepository;

import java.util.List;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final MeasurementSessionRepository measurementSessionRepository;

    public DoctorService(DoctorRepository doctorRepository, MeasurementSessionRepository measurementSessionRepository) {
        this.doctorRepository = doctorRepository;
        this.measurementSessionRepository = measurementSessionRepository;
    }

    public List<Patient> getPatientsOfDoctor(Long doctorId) {
        return doctorRepository.findPatientsByDoctorId(doctorId);
    }

    public List<MeasurementSession> getPatientSessions(Long patientId) {
        return measurementSessionRepository.findSessionsByPatientId(patientId);
    }

}
