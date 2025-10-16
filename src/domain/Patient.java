package domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
public class Patient {
    private Long id;
    private Long userId;
    private String name;
    private String lastName;
    private Gender gender;
    private LocalDate birthDate;
    private Set<Symptom> symptoms;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id) && Objects.equals(userId, patient.userId) && Objects.equals(name, patient.name) && Objects.equals(lastName, patient.lastName) && gender == patient.gender && Objects.equals(birthDate, patient.birthDate) && Objects.equals(symptoms, patient.symptoms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, name, lastName, gender, birthDate, symptoms);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                ", birthDate=" + birthDate +
                ", symptoms=" + symptoms +
                '}';
    }
}
