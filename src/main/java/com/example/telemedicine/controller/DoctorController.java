package com.example.telemedicine.controller;

import com.example.telemedicine.domain.MeasurementSession;
import com.example.telemedicine.domain.Patient;
import com.example.telemedicine.service.PatientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.telemedicine.domain.Doctor;
import com.example.telemedicine.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final PatientService patientService;

    public DoctorController(DoctorService doctorService, PatientService patientService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * Retrieves a list of all patients assigned to a specific doctor
     * @param doctorId doctorId ID of the doctor
     * @return list of patients assigned to the doctor
     */
    @GetMapping("/{doctorId}/patients")
    public List<Patient> getPatientsOfDoctor(@PathVariable Long doctorId) {
        return doctorService.getPatientsOfDoctor(doctorId);
    }

    /**
     * Retrieves all measurement sessions belonging to a specific patient
     * @param patientId the patient's ID
     * @return list of measurement sessions
     */
    @GetMapping("/sessions/patients/{patientId}/sessions")
    public List<MeasurementSession> getPatientSessions(@PathVariable Long patientId) {
        return patientService.getSessionsByPatient(patientId);
    }

    //?? REPORT: generate or view

    /**
     * Retrieves all doctors in the system
     * @return list of doctors
     */
    @GetMapping
    public List<Doctor> getAllDoctors(){
        return doctorService.getAllDoctors();
    }

    /**
     * Finds a doctor using the doctor ID
     * @param doctorId ID of the doctor
     * @return doctor object
     */
    @GetMapping("/{doctorId}")
    public Doctor findDoctorById(@PathVariable Long doctorId){
        return doctorService.findDoctorById(doctorId);
    }

    /**
     * Retrieves a list of patients who have requested approval to be assigned to this doctor
     * @param doctorId ID of the doctor
     * @return list of pending patient requests
     */
    @GetMapping("/{doctorId}/requests")
    public List<Patient> getPendingRequests(@PathVariable Long doctorId){
        return doctorService.getPendingRequests(doctorId);
    }

    /**
     * Approves a patient’s request to be assigned to the doctor
     * @param doctorId ID of the doctor
     * @param patientId ID of the patient requesting approval
     * @return updated patient object
     */
    @PostMapping("/{doctorId}/approve/{patientId}")
    public Patient acceptPatientRequest(@PathVariable Long doctorId, @PathVariable Long patientId) {
        return doctorService.approvePatientRequest(patientId, doctorId);
    }

    /**
     * Rejects a patient’s request to be assigned to the doctor
     * @param doctorId ID of the doctor
     * @param patientId ID of the patient requesting approval
     * @return updated patient object
     */
    @PostMapping("/{doctorId}/reject/{patientId}")
    public Patient rejectPatientRequest(@PathVariable Long doctorId, @PathVariable Long patientId) {
        return doctorService.rejectPatientRequest(patientId, doctorId);
    }

}
