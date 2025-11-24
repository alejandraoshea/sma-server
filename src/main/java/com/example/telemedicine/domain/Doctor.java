package com.example.telemedicine.domain;

import lombok.Data;

import java.util.Objects;

@Data
public class Doctor {
    private Long doctorID;
    private Long userId;
    private String name;
    private String lastName;
    private Gender gender;

    public Doctor(Long doctorID, Long userId, String name, String lastName, Gender gender) {
        this.doctorID = doctorID;
        this.userId = userId;
        this.name = name;
        this.lastName = lastName;
        this.gender = gender;
    }

    public Doctor(Long doctorID, String name, String lastName, Gender gender) {
        this.doctorID = doctorID;
        this.name = name;
        this.lastName = lastName;
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(doctorID, doctor.doctorID) && Objects.equals(userId, doctor.userId) && Objects.equals(name, doctor.name) && Objects.equals(lastName, doctor.lastName) && gender == doctor.gender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorID, userId, name, lastName, gender);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorID=" + doctorID +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", surname='" + lastName + '\'' +
                ", gender=" + gender +
                '}';
    }
}
