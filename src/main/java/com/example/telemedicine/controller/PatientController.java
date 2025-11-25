package com.example.telemedicine.controller;

import com.example.telemedicine.domain.Doctor;
import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.service.PatientService;
import com.example.telemedicine.service.MeasurementSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;
    private final MeasurementSessionService measurementSessionService;

    public PatientController(PatientService patientService,
                             MeasurementSessionService measurementSessionService) {
        this.patientService = patientService;
        this.measurementSessionService = measurementSessionService;
    }

    //** CRUD and see all sessions
    @GetMapping("/{patientId}/sessions")
    public List<MeasurementSession> getPatientSessions(@PathVariable Long patientId) {
        return measurementSessionService.getSessionsByPatient(patientId);
    }

    @PostMapping("/{patientId}/request/{doctorId}")
    public Doctor selectDoctorFromList(@PathVariable Long patientId, @PathVariable Long doctorId) {
        return patientService.selectDoctorFromList(patientId, doctorId);
    }

    @GetMapping("/{patientId}")
    public Patient getPatient(@PathVariable Long patientId) {
        return patientService.findById(patientId);
    }

}
