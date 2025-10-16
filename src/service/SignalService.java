package service;

import org.springframework.stereotype.Service;
import repository.SignalRepository;

@Service
public class SignalService {
    private final SignalRepository signalRepository;

    public SignalService(SignalRepository signalRepository) {
        this.signalRepository = signalRepository;
    }

}
