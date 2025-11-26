package com.example.telemedicine.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@Data
public class Report {

    private Long reportId;
    private Long patient_id;
    private Long doctor_id;
    private Long session_id;
    private byte[] fileData;
    private String fileName;
    private String fileType;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Report(Long patient_id, Long doctor_id, Long session_id, byte[] fileData, String fileName, String fileType) {
        this.patient_id = patient_id;
        this.doctor_id = doctor_id;
        this.session_id = session_id;
        this.fileData = fileData;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(reportId, report.reportId) && Objects.equals(patient_id, report.patient_id) && Objects.equals(doctor_id, report.doctor_id) && Objects.equals(session_id, report.session_id) && Arrays.equals(fileData, report.fileData) && Objects.equals(fileName, report.fileName) && Objects.equals(fileType, report.fileType) && Objects.equals(createdAt, report.createdAt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(reportId, patient_id, doctor_id, session_id, fileName, fileType, createdAt);
        result = 31 * result + Arrays.hashCode(fileData);
        return result;
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", patient_id=" + patient_id +
                ", doctor_id=" + doctor_id +
                ", session_id=" + session_id +
                ", fileData=" + Arrays.toString(fileData) +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}