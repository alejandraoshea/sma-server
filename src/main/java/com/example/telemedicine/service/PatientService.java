package com.example.telemedicine.service;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.PatientRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Submits a request for a patient to select a doctor
     * @param patientId ID of the patient
     * @param doctorId ID of the doctor
     * @return the doctor selected
     */
    public Doctor selectDoctorFromList(Long patientId, Long doctorId) {
        return patientRepository.sendDoctorRequest(patientId, doctorId);
    }

    /**
     * Retrieves a patient by ID, including their selected doctor if assigned
     * @param patientId ID of the patient
     * @return patient object
     */
    public Patient findById(Long patientId) {
        Patient p = patientRepository.findById(patientId);

        if (p.getSelectedDoctorId() != null) {
            Doctor d = doctorRepository.findDoctorById(p.getSelectedDoctorId());
            p.setSelectedDoctorId(d.getDoctorId());
        }
        return p;
    }

    /**
     * Starts a new measurement session for a patient
     * @param patientId ID of the patient starting the session
     * @return newly created measurement session
     */
    public MeasurementSession startNewSession(Long patientId) {
        return patientRepository.startNewSession(patientId);
    }

    /**
     * Stores a signal for a given session
     * @param sessionId ID of the session
     * @param signal the signal data
     * @return saved signal
     */
    public Signal uploadSignal(Long sessionId, Signal signal) {
        return patientRepository.saveSignal(sessionId, signal);
    }

    /**
     * Stores symptoms for a given session
     * @param sessionId ID of the session
     * @param symptoms the symptoms data
     * @return saved symptoms object
     */
    public Symptoms uploadSymptoms(Long sessionId, Symptoms symptoms) {
        return patientRepository.saveSymptoms(sessionId, symptoms);
    }

    /**
     * Retrieves all signals from a given session
     * @param sessionId ID of the session
     * @return list of signals
     */
    public List<Signal> getSignalsBySession(Long sessionId) {
        return patientRepository.findSignalsBySessionId(sessionId);
    }

    /**
     * Retrieves all symptoms from a given session
     * @param sessionId ID of the session
     * @return list of symptoms
     */
    public Set<SymptomType> getSymptomsBySession(Long sessionId) {
        return patientRepository.findSymptomsBySessionId(sessionId);
    }

    /**
     * Retrieves all measurement sessions for a given patient
     * @param patientId ID of the patient
     * @return list of measurement sessions
     */
    public List<MeasurementSession> getSessionsByPatient(Long patientId) {
        return patientRepository.findSessionsByPatientId(patientId);
    }

    /**
     * Uploads an EMG signal file for the session
     * @param signal raw binary file content
     * @param sessionId ID of the session
     * @return saved EMG signal
     * @throws IOException if processing fails
     */
    public Signal addEMG(byte[] signal, Long sessionId) throws IOException {
        return patientRepository.addEMG(signal, sessionId);
    }

    public Signal addECG(byte[] signal, Long sessionId) {
        return patientRepository.addECG(signal, sessionId);
    }

}
