package service;

import domain.Symptom;
import org.springframework.stereotype.Service;
import repository.SymptomRepository;

import java.util.List;

@Service
public class SymptomService {
    private final SymptomRepository symptomRepository;

    public SymptomService(SymptomRepository symptomRepository) {
        this.symptomRepository = symptomRepository;
    }

    //** recordSymptom or saveSymptom, getSymptomsForPatient,..?
    /*public void recordSymptom(Symptom symptom) {
        symptomRepository.save(symptom);
    }

    public List<Symptom> getSymptomsForPatient(Long patientId) {
        return symptomRepository.findByPatientId(patientId);
    }
     */
}
