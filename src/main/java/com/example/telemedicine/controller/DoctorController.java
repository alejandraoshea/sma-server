package com.example.telemedicine.controller;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.service.MeasurementSessionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    public List<Patient> getDoctorPatients(@PathVariable Long doctorId) {
        return doctorService.getDoctorPatients(doctorId);
    }

    @GetMapping("/patients/{patientId}/sessions")
    public List<MeasurementSession> getPatientSessions(@PathVariable Long patientId) {
        return sessionService.getSessionsByPatient(patientId);
    }

    /*
    @PostMapping("/{doctorId}/upload-prescription")
    public String uploadPrescription(
            @PathVariable Long doctorId,
            @RequestParam("patientId") Long patientId,
            @RequestParam("file") MultipartFile file) throws IOException {
        doctorService.savePrescription(doctorId, patientId, file);
        return "Prescription uploaded successfully";
    }

    @GetMapping("/{doctorId}/reports/{patientId}")
    public Object viewPatientReport(@PathVariable Long doctorId, @PathVariable Long patientId) {
        return doctorService.getPatientReport(doctorId, patientId);
    }
     */
}
