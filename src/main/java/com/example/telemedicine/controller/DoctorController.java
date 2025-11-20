package com.example.telemedicine.controller;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.service.MeasurementSessionService;
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

    public DoctorController(DoctorService doctorService, MeasurementSessionService sessionService) {
        this.doctorService = doctorService;
        this.sessionService = sessionService;
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

}
