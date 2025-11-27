package com.example.telemedicine.domain;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Data
public class Doctor {
    private Long doctorId;
    private Long userId;
    private String name;
    private String surname;
    private Gender gender;
    private List<Patient> patients;

    public Doctor(Long doctorID, Long userId, String name, String surname, Gender gender, List<Patient> patients) {
        this.doctorId = doctorID;
        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.patients = patients;
    }

    public Doctor(Long doctorID, Long userId, String name, String surname, Gender gender) {
        this.doctorId = doctorID;
        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.patients = new LinkedList<>();
    }

    public Doctor(Long doctorID, String name, String surname, Gender gender) {
        this.doctorId = doctorID;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(doctorId, doctor.doctorId) && Objects.equals(userId, doctor.userId) && Objects.equals(name, doctor.name) && Objects.equals(surname, doctor.surname) && gender == doctor.gender && Objects.equals(patients, doctor.patients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorId, userId, name, surname, gender, patients);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId=" + doctorId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", gender=" + gender +
                ", patients=" + patients +
                '}';
    }
}
