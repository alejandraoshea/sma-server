package com.example.telemedicine.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Data
public class Patient {
    private Long patientID;
    private Long userId;
    private String name;
    private String lastName;
    private Gender gender;
    private LocalDate birthDate;
    private Set<Symptoms> symptoms;
    private Long height; // in cm
    private double weight; // kg

    public Patient(Long patientID, Long userId, String name, String lastName, Gender gender, LocalDate birthDate, Set<Symptoms> symptoms, Long height, double weight) {
        this.patientID = patientID;
        this.userId = userId;
        this.name = name;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.symptoms = symptoms;
        this.height = height;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Double.compare(patient.weight, weight) == 0 && Objects.equals(patientID, patient.patientID) && Objects.equals(userId, patient.userId) && Objects.equals(name, patient.name) && Objects.equals(lastName, patient.lastName) && gender == patient.gender && Objects.equals(birthDate, patient.birthDate) && Objects.equals(symptoms, patient.symptoms) && Objects.equals(height, patient.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientID, userId, name, lastName, gender, birthDate, symptoms, height, weight);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + patientID +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                ", birthDate=" + birthDate +
                ", symptoms=" + symptoms +
                ", height=" + height +
                ", weight=" + weight +
                '}';
    }
}
