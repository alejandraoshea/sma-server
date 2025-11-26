package com.example.telemedicine.domain;

import java.time.LocalDateTime;

public class Report {

    private Long reportId;
    private Long patient_id;
    private Long doctor_id;
    private Long session_id;
    private byte[] fileData;
    private String fileName;
    private String fileType;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Report() {
    }

    public Report(Long patient_id, Long doctor_id, Long session_id, byte[] fileData, String fileName, String fileType) {
        this.patient_id = patient_id;
        this.doctor_id = doctor_id;
        this.session_id = session_id;
        this.fileData = fileData;
        this.fileName = fileName;
        this.fileType = fileType;
    }

}