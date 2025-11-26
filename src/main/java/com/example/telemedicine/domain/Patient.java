package com.example.telemedicine.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Data
public class Patient {
    private Long patientId;
    private Long userId;
    private String name;
    private String surname;
    private Gender gender;
    private LocalDate birthDate;
    private Long height; // in cm
    private double weight; // kg
    private List<MeasurementSession> measurementSessionList; //contains symptoms and signals (list to preserve insertion order)
    private Long doctorId;
    private Long selectedDoctorId; //id of the doctor selected by the patient
    private DoctorApprovalStatus doctorApprovalStatus; //status of the doctor's request

    public Patient() {
        this.name = "";
        this.surname = "";
        this.gender = Gender.MALE;
        this.birthDate = null;
        this.height = 0L;
        this.weight = 0;
        this.measurementSessionList = new LinkedList<>();
    }

    public Patient(Long patientID, Long userId, String name, String lastName, Gender gender, LocalDate birthDate,
                   Long height, double weight, List<MeasurementSession> measurementSessionList) {
        this.patientId = patientID;
        this.userId = userId;
        this.name = name;
        this.surname = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.measurementSessionList = measurementSessionList;
    }

    public Patient(Long patientID, Long userId, String name, String lastName, Gender gender, LocalDate birthDate, Long height,
                   double weight, List<MeasurementSession> measurementSessionList, Long doctorId, Long selectedDoctorId, DoctorApprovalStatus doctorApprovalStatus) {
        this.patientId = patientID;
        this.userId = userId;
        this.name = name;
        this.surname = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.measurementSessionList = measurementSessionList;
        this.doctorId = doctorId;
        this.selectedDoctorId = selectedDoctorId;
        this.doctorApprovalStatus = doctorApprovalStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Double.compare(patient.weight, weight) == 0 && Objects.equals(patientId, patient.patientId) && Objects.equals(userId, patient.userId) && Objects.equals(name, patient.name) && Objects.equals(surname, patient.surname) && gender == patient.gender && Objects.equals(birthDate, patient.birthDate) && Objects.equals(height, patient.height) && Objects.equals(measurementSessionList, patient.measurementSessionList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId, userId, name, surname, gender, birthDate, height, weight, measurementSessionList);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", gender=" + gender +
                ", birthDate=" + birthDate +
                ", height=" + height +
                ", weight=" + weight +
                ", measurementSessionList=" + measurementSessionList +
                ", selectedDoctorId=" + selectedDoctorId +
                ", doctorApprovalStatus=" + doctorApprovalStatus +
                '}';
    }
}

