package com.example.telemedicine.service;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.exceptions.PdfGeneratorException;
import com.example.telemedicine.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.DoctorRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PdfGenerator pdfGenerator;

    public DoctorService(DoctorRepository doctorRepository, PatientRepository patientRepository, PdfGenerator pdfGenerator) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.pdfGenerator = pdfGenerator;
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

    public Report generateReport(Long doctorId, Long sessionId) {
        MeasurementSession session = patientRepository.findSessionsById(sessionId);
        Patient patient = patientRepository.findById(session.getPatientId());
        Set<SymptomType> symptoms = patientRepository.findSymptomsBySessionId(sessionId);
        List<Signal> signals = patientRepository.findSignalsBySessionId(sessionId);

        byte[] pdfBytes = new byte[0];
        try {
            pdfBytes = pdfGenerator.generateSessionPDF(patient, session, symptoms, signals);
        } catch (PdfGeneratorException e) {
            throw new RuntimeException(e);
        }

        Report report = new Report(patient.getPatientId(), doctorId, sessionId, pdfBytes,
                "session_" + sessionId + "_report.pdf", "application/pdf"
        );

        return doctorRepository.saveReport(report);
    }

    /** Fetch stored report PDF */
    public Report getReport(Long reportId) {
        return doctorRepository.findReportById(reportId);
    }

    public List<Locality> getAllLocalities() {
        return doctorRepository.getAllLocalities();
    }

    public Locality addLocality(Locality locality) {
        return doctorRepository.insertLocality(locality);
    }

}
