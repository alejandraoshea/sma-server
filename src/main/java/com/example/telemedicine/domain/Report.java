package com.example.telemedicine.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@Data
public class Report {
    private Long reportId;
    private Long patientId;
    private Long doctorId;
    private Long sessionId;
    private byte[] fileData;
    private String fileName;
    private String fileType;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Report(Long reportId, Long patientId, Long doctorId, Long sessionId, byte[] fileData, String fileName, String fileType) {
        this.reportId = reportId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.sessionId = sessionId;
        this.fileData = fileData;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public Report(Long patientId, Long doctorId, Long sessionId, byte[] fileData, String fileName, String fileType) {
        this.patientId = patientId;;
        this.doctorId = doctorId;;
        this.sessionId = sessionId;;
        this.fileData = fileData;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(reportId, report.reportId) && Objects.equals(patientId, report.patientId) && Objects.equals(doctorId, report.doctorId) && Objects.equals(sessionId, report.sessionId) && Arrays.equals(fileData, report.fileData) && Objects.equals(fileName, report.fileName) && Objects.equals(fileType, report.fileType) && Objects.equals(createdAt, report.createdAt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(reportId, patientId, doctorId, sessionId, fileName, fileType, createdAt);
        result = 31 * result + Arrays.hashCode(fileData);
        return result;
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", sessionId=" + sessionId +
                ", fileData=" + Arrays.toString(fileData) +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}