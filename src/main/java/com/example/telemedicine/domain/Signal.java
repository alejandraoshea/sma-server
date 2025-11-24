package com.example.telemedicine.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class Signal {
    private Long id;
    private Long measurementSessionId; //** FK measurementSession
    private LocalDateTime timestamp;
    private SignalType signalType;
    private String patientSignalData;
    private int fs;

    public Signal(Long id, Long measurementSessionId, LocalDateTime timestamp, SignalType signalType, String patientSignalData, int fs) {
        this.id = id;
        this.measurementSessionId = measurementSessionId;
        this.timestamp = timestamp;
        this.signalType = signalType;
        this.patientSignalData = patientSignalData;
        this.fs = fs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signal signal = (Signal) o;
        return fs == signal.fs && Objects.equals(id, signal.id) && Objects.equals(measurementSessionId, signal.measurementSessionId) && Objects.equals(timestamp, signal.timestamp) && signalType == signal.signalType && Objects.equals(patientSignalData, signal.patientSignalData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, measurementSessionId, timestamp, signalType, patientSignalData, fs);
    }

    @Override
    public String toString() {
        return "Signal{" +
                "id=" + id +
                ", measurementSessionId=" + measurementSessionId +
                ", timestamp=" + timestamp +
                ", signalType=" + signalType +
                ", patientSignalData='" + patientSignalData + '\'' +
                ", fs=" + fs +
                '}';
    }
}
