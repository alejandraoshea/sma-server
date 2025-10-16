package domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

//** recording session grouping multiple signals
public class MeasurementSession {
    private Integer id;
    private Long patientId;
    private LocalDateTime timeStamp;
    private List<Signal> signals;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeasurementSession that = (MeasurementSession) o;
        return Objects.equals(id, that.id) && Objects.equals(patientId, that.patientId) && Objects.equals(timeStamp, that.timeStamp) && Objects.equals(signals, that.signals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientId, timeStamp, signals);
    }

    @Override
    public String toString() {
        return "MeasurementSession{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", timeStamp=" + timeStamp +
                ", signals=" + signals +
                '}';
    }
}
