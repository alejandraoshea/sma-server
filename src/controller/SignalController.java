package controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import service.SignalService;
import domain.Signal;

@RestController
@RequestMapping("/api/signals")
@CrossOrigin(origins = "*")

public class SignalController {
    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    /*
    @PostMapping("/{patientId}")
    public Signal uploadSignal(@PathVariable Long patientId, @RequestBody Signal signal) {
        return signalService.saveSignal(patientId, signal);
    }

    @GetMapping("/{patientId}")
    public List<Signal> getSignals(@PathVariable Long patientId) {
        return signalService.getSignalsByPatient(patientId);
    }

     */

}
