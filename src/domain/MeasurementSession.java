package domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PhysiologicalMeasurement {
    private Integer id;
    private Long patientId;
    private List<Signal> signals;
    private LocalDateTime timeStamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysiologicalMeasurement that = (PhysiologicalMeasurement) o;
        return Objects.equals(id, that.id) && Objects.equals(patientId, that.patientId) && Objects.equals(signals, that.signals) && Objects.equals(timeStamp, that.timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientId, signals, timeStamp);
    }

    @Override
    public String toString() {
        return "PhysiologicalMeasurement{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", signals=" + signals +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
