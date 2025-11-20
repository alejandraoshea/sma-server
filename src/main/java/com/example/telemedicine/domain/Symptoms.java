package com.example.telemedicine.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Data
public class Symptoms {
    private Long symptomId;
    private Long measurementSessionId;
    private LocalDateTime timestamp;
    private Set<SymptomType> symptomsSet;

    public Symptoms(Long symptomId, Long measurementSessionId, Set<SymptomType> symptomsSet, LocalDateTime timestamp) {
        this.symptomId = symptomId;
        this.measurementSessionId = measurementSessionId;
        this.symptomsSet = symptomsSet;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symptoms symptoms = (Symptoms) o;
        return Objects.equals(symptomId, symptoms.symptomId) && Objects.equals(measurementSessionId, symptoms.measurementSessionId) && Objects.equals(symptomsSet, symptoms.symptomsSet) && Objects.equals(timestamp, symptoms.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symptomId, measurementSessionId, symptomsSet, timestamp);
    }

    @Override
    public String toString() {
        return "Symptoms{" +
                "symptomId=" + symptomId +
                ", measurementSessionId=" + measurementSessionId +
                ", symptomsSet=" + symptomsSet +
                ", timestamp=" + timestamp +
                '}';
    }
}
