package com.example.telemedicine.service;

import com.example.telemedicine.domain.*;
import com.example.telemedicine.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import com.example.telemedicine.repository.PatientRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
     *
     * @param patientId ID of the patient
     * @param doctorId  ID of the doctor
     * @return the doctor selected
     */
    public Doctor selectDoctorFromList(Long patientId, Long doctorId) {
        return patientRepository.sendDoctorRequest(patientId, doctorId);
    }

    /**
     * Updates patient's personal information according to form
     *
     * @param patientId
     * @param newData
     */
    public void updatePatientInfo(Long patientId, Patient newData) {
        int updated = patientRepository.updatePatientInfo(patientId, newData);
        if (updated == 0) throw new RuntimeException("Patient not found");

        patientRepository.findById(patientId);
    }

    /**
     * Retrieves a patient by ID, including their selected doctor if assigned
     *
     * @param patientId ID of the patient
     * @return patient object
     */
    public Patient findById(Long patientId) {
        Patient patient = patientRepository.findById(patientId);

        if (patient.getSelectedDoctorId() != null) {
            Doctor d = doctorRepository.findDoctorById(patient.getSelectedDoctorId());
            patient.setSelectedDoctorId(d.getDoctorId());
        }

        return patient;
    }

    /**
     * Starts a new measurement session for a patient
     *
     * @param patientId ID of the patient starting the session
     * @return newly created measurement session
     */
    public MeasurementSession startNewSession(Long patientId) {
        return patientRepository.startNewSession(patientId);
    }

    /**
     * Stores a signal for a given session
     *
     * @param sessionId ID of the session
     * @param signal    the signal data
     * @return saved signal
     */
    public Signal uploadSignal(Long sessionId, Signal signal) {
        return patientRepository.saveSignal(sessionId, signal);
    }

    /**
     * Stores symptoms for a given session
     *
     * @param sessionId ID of the session
     * @param symptoms  the symptoms data
     * @return saved symptoms object
     */
    public Set<SymptomType> uploadSymptoms(Long sessionId, Set<SymptomType> symptoms) {
        return patientRepository.saveSymptoms(sessionId, symptoms);
    }

    /**
     * Retrieves all signals from a given session
     *
     * @param sessionId ID of the session
     * @return list of signals
     */
    public List<Signal> getSignalsBySession(Long sessionId) {
        return patientRepository.findSignalsBySessionId(sessionId);
    }

    /**
     * Retrieves all symptoms from a given session
     *
     * @param sessionId ID of the session
     * @return list of symptoms
     */
    public Set<SymptomType> getSymptomsBySession(Long sessionId) {
        return patientRepository.findSymptomsBySessionId(sessionId);
    }

    /**
     * Retrieves all measurement sessions for a given patient
     *
     * @param patientId ID of the patient
     * @return list of measurement sessions
     */
    public List<MeasurementSession> getSessionsByPatient(Long patientId) {
        return patientRepository.findSessionsByPatientId(patientId);
    }

    /**
     * Uploads an EMG signal file for the session
     *
     * @param signal    raw binary file content
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


    public void generateAndSaveCsvSummary(Long sessionId) {
        MeasurementSession session = patientRepository.findSessionsById(sessionId);
        if (session == null) throw new IllegalArgumentException("Session not found");

        Patient patient = patientRepository.findById(session.getPatientId());
        if (patient == null) throw new IllegalArgumentException("Patient not found");

        List<Signal> signals = patientRepository.findSignalsBySessionId(sessionId);

        StringBuilder csv = new StringBuilder();

        csv.append("Patient Info\n");
        csv.append("Name,Surname,Gender,BirthDate,Height,Weight\n");
        csv.append(String.format("%s,%s,%s,%s,%d,%.2f\n",
                patient.getName(),
                patient.getSurname(),
                patient.getGender() != null ? patient.getGender().name() : "",
                patient.getBirthDate() != null ? patient.getBirthDate().toString() : "",
                patient.getHeight() != null ? patient.getHeight() : 0,
                patient.getWeight()
        ));
        csv.append("\n");

        for (Signal signal : signals) {
            if (signal.getSignalType() == SignalType.ECG || signal.getSignalType() == SignalType.EMG) {
                csv.append(signal.getSignalType().name()).append(" Signal\n");
                csv.append("SignalId,Timestamp,Fs,Data\n");
                csv.append(String.format("%d,%s,%d,\"%s\"\n",
                        signal.getId(),
                        signal.getTimestamp() != null ? signal.getTimestamp().toString() : "",
                        signal.getFs(),
                        signal.getPatientSignalData().replace("\n", " ").replace("\"", "\"\"")
                ));
                csv.append("\n");
            }
        }

        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        patientRepository.saveCsvSummaryFile(sessionId, csvBytes, "session_" + sessionId + "_file.csv", "text/csv");
    }

    public byte[] getCsvSummaryFile(Long sessionId) {
        return patientRepository.getCsvSummaryFile(sessionId);
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        String escaped = input.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
        return escaped;
    }

    public List<Doctor> getDoctorsForMap(Long patientId) {
        return patientRepository.getDoctorsForMap(patientId);
    }

    public List<Report> findReportsByPatientId(Long patientId) {
        return patientRepository.getAllReports(patientId);
    }

    public Report findReportByReportId(Long reportId) {
        return patientRepository.getSingleReport(reportId);
    }
}
