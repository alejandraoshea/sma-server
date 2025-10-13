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



}
