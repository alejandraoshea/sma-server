package com.example.telemedicine.controller;

import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.domain.Symptoms;
import com.example.telemedicine.service.PatientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    //** patient can submit or update their symptoms
    @PostMapping("/{patientId}/symptoms")
    public List<Symptoms> updateSymptoms(@PathVariable Long patientId, @RequestBody List<Symptoms> symptoms){
        return patientService.updateSymptoms(patientId, symptoms);
    }

    //** see all symptoms
    @GetMapping("/{patientId}/symptoms")
    public List<Symptoms> getSymptoms(@PathVariable Long patientId) {
        return patientService.getSymptoms(patientId);
    }

    //!! will be handling the symptoms for a cleaner REST API design
}
