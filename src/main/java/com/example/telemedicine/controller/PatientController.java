package com.example.telemedicine.controller;

import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.service.PatientService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /*
    @PostMapping("/{patientId}/symptoms")
    public ArrayList<String> submitSymptom(@PathVariable Long patientId, @RequestBody ArrayList<String> symptom) {
        return patientService.saveSymptom(patientId, symptom);
    }

    @GetMapping("/{patientId}/symptoms")
    public List<String> getSymptoms(@PathVariable Long patientId) {
        return patientService.getSymptoms(patientId);
    }

    //** recordSymptom: POST
    //** getPatientSymptoms : GET


        @PostMapping
    public ResponseEntity<String> recordSymptom(@RequestBody Symptom symptom) {
        symptomService.recordSymptom(symptom);
        return ResponseEntity.ok("Symptom recorded successfully");
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<Symptom>> getPatientSymptoms(@PathVariable Long id) {
        return ResponseEntity.ok(symptomService.getSymptomsForPatient(id));
    }
     */


    //!! will be handling the symptoms for a cleaner REST API design
}
