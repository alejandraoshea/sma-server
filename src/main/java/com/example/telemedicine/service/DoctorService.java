package com.example.telemedicine.service;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.domain.Doctor;
import com.example.telemedicine.repository.PatientRepository;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.DoctorRepository;

import java.util.List;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public DoctorService(DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public List<Patient> getPatientsOfDoctor(Long doctorId) {
        return doctorRepository.findPatientsByDoctorId(doctorId);
    }

    /**
     * Updates doctor's personal information according to form
     *
     * @param doctorId
     * @param newData
     */
    public void updateDoctorInfo(Long doctorId, Doctor newData) {
        int updated = doctorRepository.updateDoctorInfo(doctorId, newData);
        if (updated == 0) throw new RuntimeException("Doctor not found");

        doctorRepository.findDoctorById(doctorId);
    }

    public List<MeasurementSession> getPatientSessions(Long patientId) {
        return patientRepository.findSessionsByPatientId(patientId);
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
