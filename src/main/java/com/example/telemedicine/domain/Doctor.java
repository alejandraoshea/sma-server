package com.example.telemedicine.domain;

import lombok.Data;

import java.util.Objects;

@Data
public class Doctor {
    private Long doctorId;
    private Long userId;
    private String name;
    private String surname;
    private Gender gender;

    public Doctor(Long doctorID, Long userId, String name, String lastName, Gender gender) {
        this.doctorId = doctorID;
        this.userId = userId;
        this.name = name;
        this.surname = lastName;
        this.gender = gender;
    }

    public Doctor(Long doctorID, String name, String lastName, Gender gender) {
        this.doctorId = doctorID;
        this.name = name;
        this.surname = lastName;
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(doctorId, doctor.doctorId) && Objects.equals(userId, doctor.userId) && Objects.equals(name, doctor.name) && Objects.equals(surname, doctor.surname) && gender == doctor.gender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorId, userId, name, surname, gender);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId=" + doctorId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", gender=" + gender +
                '}';
    }
}
