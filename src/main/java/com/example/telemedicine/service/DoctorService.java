package com.example.telemedicine.service;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.domain.Doctor;
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

    public List<Doctor> getAllDoctors() {
        return doctorRepository.getAllDoctors();
    }

    public Patient approvePatientRequest(Long patientId, Long doctorId) {
        return doctorRepository.approvePatientRequest(patientId, doctorId);
    }

    public Patient rejectPatientRequest(Long patientId, Long doctorId) {
        return doctorRepository.rejectPatientRequest(patientId, doctorId);
    }

    public List<Patient> getPendingRequests(Long doctorId) {
        return doctorRepository.getPendingRequests(doctorId);
    }

    public Doctor findDoctorById(Long doctorId){
        return doctorRepository.findDoctorById(doctorId);
    }
}
