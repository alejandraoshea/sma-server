package domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Symptom {
    private Long id;
    private Long patientId;
    private SymptomType type;
    private String value; //? example: 38.5ºC for fever or moderate for weakness??
    private LocalDateTime timestamp;

    public Symptom(Long id, Long patientId, SymptomType type, String value, LocalDateTime timestamp) {
        this.id = id;
        this.patientId = patientId;
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symptom symptom = (Symptom) o;
        return Objects.equals(id, symptom.id) && Objects.equals(patientId, symptom.patientId) && type == symptom.type && Objects.equals(value, symptom.value) && Objects.equals(timestamp, symptom.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientId, type, value, timestamp);
    }

    @Override
    public String toString() {
        return "Symptom{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", type=" + type +
                ", value='" + value + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
