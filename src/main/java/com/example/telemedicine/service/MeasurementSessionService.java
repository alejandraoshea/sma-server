package com.example.telemedicine.service;

import com.example.telemedicine.domain.Signal;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.SignalRepository;

import java.util.List;

@Service
public class SignalService {
    private final SignalRepository signalRepository;

    public SignalService(SignalRepository signalRepository) {
        this.signalRepository = signalRepository;
    }

    //** we could do:
    /*
    public void saveSignal(Signal signal) {
        signalRepository.save(signal);
    }

    public List<Signal> getSignalsByPatient(Long patientId) {
        return signalRepository.findByPatientId(patientId);
    }
     */

}
