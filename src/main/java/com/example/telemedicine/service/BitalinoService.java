package com.example.telemedicine.service;

import org.springframework.stereotype.Service;
import com.example.telemedicine.bitalino.BITalino;
import com.example.telemedicine.bitalino.Frame;
import com.example.telemedicine.bitalino.BITalinoException;

@Service
public class BitalinoService {

    private BITalino bitalino;

    public void connect(String macAddress) throws Throwable {
        bitalino = new BITalino();
        bitalino.open(macAddress, 1000); // 1000 Hz sampling
        System.out.println("Connected to BITalino at " + macAddress);
    }

    public void startAcquisition(int[] channels) throws Throwable {
        if (bitalino == null) throw new IllegalStateException("Not connected");
        bitalino.start(channels);
    }

    public Frame[] readSamples(int nSamples) throws BITalinoException {
        if (bitalino == null) throw new IllegalStateException("Not connected");
        return bitalino.read(nSamples);
    }

    public void stopAcquisition() throws BITalinoException {
        if (bitalino != null) {
            bitalino.stop();
        }
    }

    public void disconnect() throws BITalinoException {
        if (bitalino != null) {
            bitalino.close();
        }
    }
}