package com.example.telemedicine.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

//** recording session grouping multiple signals
@Data
public class MeasurementSession {
    private Long sessionId;
    private Long patientId;
    private LocalDateTime timeStamp;
    private Set<SymptomType> symptomsSet;
    private List<Signal> signals;

    public MeasurementSession(Long sessionId, Long patientId, LocalDateTime timeStamp, Set<SymptomType> symptomsSet, List<Signal> signals) {
        this.sessionId = sessionId;
        this.patientId = patientId;
        this.timeStamp = timeStamp;
        this.symptomsSet = symptomsSet;
        this.signals = signals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeasurementSession that = (MeasurementSession) o;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(patientId, that.patientId) && Objects.equals(timeStamp, that.timeStamp) && Objects.equals(symptomsSet, that.symptomsSet) && Objects.equals(signals, that.signals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, patientId, timeStamp, symptomsSet, signals);
    }

    @Override
    public String toString() {
        return "MeasurementSession{" +
                "sessionId=" + sessionId +
                ", patientId=" + patientId +
                ", timeStamp=" + timeStamp +
                ", symptomsSet=" + symptomsSet +
                ", signals=" + signals +
                '}';
    }
}
