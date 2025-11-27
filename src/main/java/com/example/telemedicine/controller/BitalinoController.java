package com.example.telemedicine.controller;

import org.springframework.web.bind.annotation.*;
import com.example.telemedicine.service.BitalinoService;
import com.example.telemedicine.bitalino.Frame;

@RestController
@RequestMapping("/bitalino")
public class BitalinoController {

    private final BitalinoService bitalinoService;

    public BitalinoController(BitalinoService bitalinoService) {
        this.bitalinoService = bitalinoService;
    }

    @PostMapping("/connect")
    public String connect(@RequestParam String macAddress) {
        try {
            bitalinoService.connect(macAddress);
            return "Connected!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/start")
    public String start(@RequestBody int[] channels) {
        try {
            bitalinoService.startAcquisition(channels);
            return "Acquisition started!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/read")
    public Frame[] read(@RequestParam int samples) {
        try {
            return bitalinoService.readSamples(samples);
        } catch (Exception e) {
            e.printStackTrace();
            return new Frame[0];
        }
    }

    @PostMapping("/stop")
    public String stop() {
        try {
            bitalinoService.stopAcquisition();
            return "Acquisition stopped!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/disconnect")
    public String disconnect() {
        try {
            bitalinoService.disconnect();
            return "Disconnected!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}