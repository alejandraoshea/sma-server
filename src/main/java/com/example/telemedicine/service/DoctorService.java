package com.example.telemedicine.service;

import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.DoctorRepository;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }
}
