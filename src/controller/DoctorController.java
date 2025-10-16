package controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import domain.Doctor;
import service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /*
    @PostMapping("/{doctorId}/upload-prescription")
    public String uploadPrescription(
            @PathVariable Long doctorId,
            @RequestParam("patientId") Long patientId,
            @RequestParam("file") MultipartFile file) throws IOException {
        doctorService.savePrescription(doctorId, patientId, file);
        return "Prescription uploaded successfully";
    }

    @GetMapping("/{doctorId}/reports/{patientId}")
    public Object viewPatientReport(@PathVariable Long doctorId, @PathVariable Long patientId) {
        return doctorService.getPatientReport(doctorId, patientId);
    }

     */
}
