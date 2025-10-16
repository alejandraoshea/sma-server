package controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.SymptomService;

@RestController
@RequestMapping("/api/symptoms")
public class SymptomController {
    private final SymptomService symptomService;

    public SymptomController(SymptomService symptomService) {
        this.symptomService = symptomService;
    }

    //** recordSymptom: POST
    //** getPatientSymptoms : GET

    /*
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
}
