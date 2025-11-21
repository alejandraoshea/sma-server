package com.example.telemedicine.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
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
    private Long height; // in cm
    private double weight; // kg
    private List<MeasurementSession> measurementSessionList; //contains symptoms and signals (list to preserve insertion order)
    private Long selectedDoctorId; //id of the doctor selected by the patient
    private DoctorApprovalStatus doctorApprovalStatus; //status of the doctor's request


    public Patient(Long patientID, Long userId, String name, String lastName, Gender gender, LocalDate birthDate,
                   Long height, double weight, List<MeasurementSession> measurementSessionList) {
        this.patientID = patientID;
        this.userId = userId;
        this.name = name;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.measurementSessionList = measurementSessionList;
    }

    public Patient(Long patientID, Long userId, String name, String lastName, Gender gender, LocalDate birthDate, Long height,
                   double weight, List<MeasurementSession> measurementSessionList, Long selectedDoctorId, DoctorApprovalStatus doctorApprovalStatus) {
        this.patientID = patientID;
        this.userId = userId;
        this.name = name;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.measurementSessionList = measurementSessionList;
        this.selectedDoctorId = selectedDoctorId;
        this.doctorApprovalStatus = doctorApprovalStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Double.compare(patient.weight, weight) == 0 && Objects.equals(patientID, patient.patientID) && Objects.equals(userId, patient.userId) && Objects.equals(name, patient.name) && Objects.equals(lastName, patient.lastName) && gender == patient.gender && Objects.equals(birthDate, patient.birthDate) && Objects.equals(height, patient.height) && Objects.equals(measurementSessionList, patient.measurementSessionList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientID, userId, name, lastName, gender, birthDate, height, weight, measurementSessionList);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientID=" + patientID +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
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

