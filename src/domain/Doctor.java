package domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Data
public class Doctor {
    private Long id;
    private Long userId;
    private String name;
    private String lastName;
    private Gender gender;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(id, doctor.id) && Objects.equals(userId, doctor.userId) && Objects.equals(name, doctor.name) && Objects.equals(lastName, doctor.lastName) && gender == doctor.gender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, name, lastName, gender);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                '}';
    }
}
