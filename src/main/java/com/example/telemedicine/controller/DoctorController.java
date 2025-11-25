package com.example.telemedicine.controller;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.service.MeasurementSessionService;
import com.example.telemedicine.service.PatientService;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

import com.example.telemedicine.domain.Doctor;
import com.example.telemedicine.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final MeasurementSessionService sessionService;
    private final PatientService patientService;

    public DoctorController(DoctorService doctorService, MeasurementSessionService sessionService, PatientService patientService) {
        this.doctorService = doctorService;
        this.sessionService = sessionService;
        this.patientService = patientService;
    }

    @GetMapping("/{doctorId}/patients")
    public List<Patient> getPatientsOfDoctor(@PathVariable Long doctorId) {
        return doctorService.getPatientsOfDoctor(doctorId);
    }

    @GetMapping("/patients/{patientId}/sessions")
    public List<MeasurementSession> getPatientSessions(@PathVariable Long patientId) {
        return sessionService.getSessionsByPatient(patientId);
    }

    //?? REPORT: generate or view
    //?? PRESCRIPTION: generate or view

    //?? CHOOSE DOCTOR? : Mostrar doctores y luego elegir uno disponible (2 distintos)

    @GetMapping
    public List<Doctor> getAllDoctors(){
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{doctorId}/requests")
    public List<Patient> getPendingRequests(@PathVariable Long doctorId){
        return doctorService.getPendingRequests(doctorId);
    }

    @PostMapping("/{doctorId}/approve/{patientId}")
    public Patient acceptPatientRequest(@PathVariable Long doctorId, @PathVariable Long patientId) {
        return doctorService.approvePatientRequest(patientId, doctorId);
    }

    @PostMapping("/{doctorId}/reject/{patientId}")
    public Patient rejectPatientRequest(@PathVariable Long doctorId, @PathVariable Long patientId) {
        return doctorService.rejectPatientRequest(patientId, doctorId);
    }
}
