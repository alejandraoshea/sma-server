package service;

import domain.Symptoms;
import org.springframework.stereotype.Service;
import repository.PatientRepository;

import java.util.List;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Symptoms> updateSymptoms(Long patientId, List<Symptoms> symptoms) {
        //** assign patientId to each symptom
        for(Symptoms symptom : symptoms){
            symptom.setPatientId(patientId);
        }
        patientRepository.saveSymptoms(symptoms);
        return patientRepository.findByPatientId(symptoms);
    }

    public List<Symptoms> getSymptoms(Long patientId) {
        return patientRepository.findByPatientId(patientId);
    }

}
