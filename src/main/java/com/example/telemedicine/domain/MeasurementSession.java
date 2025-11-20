package com.example.telemedicine.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

//** recording session grouping multiple signals
public class MeasurementSession {
    private Long sessionId;
    private Long patientId;
    private LocalDateTime timeStamp;
    private Symptoms symptoms;
    private List<Signal> signals;

    public MeasurementSession(Long sessionId, Long patientId, LocalDateTime timeStamp, Symptoms symptoms, List<Signal> signals) {
        this.sessionId = sessionId;
        this.patientId = patientId;
        this.timeStamp = timeStamp;
        this.symptoms = symptoms;
        this.signals = signals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeasurementSession that = (MeasurementSession) o;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(patientId, that.patientId) && Objects.equals(timeStamp, that.timeStamp) && Objects.equals(symptoms, that.symptoms) && Objects.equals(signals, that.signals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, patientId, timeStamp, symptoms, signals);
    }

    @Override
    public String toString() {
        return "MeasurementSession{" +
                "sessionId=" + sessionId +
                ", patientId=" + patientId +
                ", timeStamp=" + timeStamp +
                ", symptoms=" + symptoms +
                ", signals=" + signals +
                '}';
    }
}
