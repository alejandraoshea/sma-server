package com.example.telemedicine.controller;

import com.example.telemedicine.domain.MeasurementSession;
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


    //CRUD and see all sessions

    @GetMapping("/{patientId}/sessions")
    public List<MeasurementSession> getPatientSessions(@PathVariable Long patientId) {
        return measurementSessionService.getSessionsByPatient(patientId);
    }

    //?? CHOOSE DOCTOR? : Mostrar doctores y luego elegir uno disponible (2 distintos)
}
