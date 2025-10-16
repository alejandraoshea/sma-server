package domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class Signal {
    private Long id;
    private Long patientId; //** patient
    private Long measurementSessionId; //** FK measurementSession
    private LocalDateTime timestamp;
    private SignalType signalType;
    private String patientData;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signal signal = (Signal) o;
        return Objects.equals(id, signal.id) && Objects.equals(patientId, signal.patientId) && Objects.equals(measurementSessionId, signal.measurementSessionId) && Objects.equals(timestamp, signal.timestamp) && signalType == signal.signalType && Objects.equals(patientData, signal.patientData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientId, measurementSessionId, timestamp, signalType, patientData);
    }

    @Override
    public String toString() {
        return "Signal{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", measurementSessionId=" + measurementSessionId +
                ", timestamp=" + timestamp +
                ", signalType=" + signalType +
                ", patientData='" + patientData + '\'' +
                '}';
    }
}
