package com.example.telemedicine.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.telemedicine.service.SignalService;
import com.example.telemedicine.domain.Signal;

@RestController
@RequestMapping("/api/signals")
@CrossOrigin(origins = "*")
public class SignalController {
    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    /*
    @PostMapping
    public ResponseEntity<String> uploadSignal(@RequestBody Signal signal) {
        signalService.saveSignal(signal);
        return ResponseEntity.ok("Signal saved");
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<Signal>> getPatientSignals(@PathVariable Long id) {
        return ResponseEntity.ok(signalService.getSignalsByPatient(id));
    }
     */

}
