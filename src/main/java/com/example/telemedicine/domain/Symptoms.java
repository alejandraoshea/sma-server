package com.example.telemedicine.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Data
public class Symptoms {
    private Long symptomId;
    private Long patientId;
    private Set<SymptomType> symptomsSet;
    private LocalDateTime timestamp;

    public Symptoms(Long symptomId, Long patientId, Set<SymptomType> symptomsSet, LocalDateTime timestamp) {
        this.symptomId = symptomId;
        this.patientId = patientId;
        this.symptomsSet = symptomsSet;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symptoms symptoms = (Symptoms) o;
        return Objects.equals(symptomId, symptoms.symptomId) && Objects.equals(patientId, symptoms.patientId) && Objects.equals(symptomsSet, symptoms.symptomsSet) && Objects.equals(timestamp, symptoms.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symptomId, patientId, symptomsSet, timestamp);
    }

    @Override
    public String toString() {
        return "Symptoms{" +
                "symptomId=" + symptomId +
                ", patientId=" + patientId +
                ", symptomsSet=" + symptomsSet +
                ", timestamp=" + timestamp +
                '}';
    }
}
