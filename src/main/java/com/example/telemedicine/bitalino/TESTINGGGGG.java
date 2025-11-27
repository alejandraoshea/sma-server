package com.example.telemedicine.bitalino;

import java.util.Vector;

public class TESTINGGGGG {

    public static void main(String[] args) {
        // Create an instance of BITalino
        BITalino bitalino = new BITalino();

        // Override findDevices() to simulate discovering devices locally
        try {
            Vector<String> devices = bitalino.findDevices();

            // If no real device found, simulate a fake device
            if (devices.isEmpty()) {
                System.out.println("No real BITalino devices found. Using mock device...");
                devices = new Vector<>();
                devices.add("/dev/cu.BITalino-XX-XX-DevB"); // simulated port
            }

            System.out.println("Discovered devices:");
            for (String port : devices) {
                System.out.println(" - " + port);
            }

            // Optionally, attempt to "open" the first device (will fail safely if not real)
            try {
                bitalino.open(devices.firstElement());
                System.out.println("Opened device at " + devices.firstElement());
            } catch (BITalinoException e) {
                System.out.println("Failed to open device (expected in mock): " + e.getMessage());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

        } catch (InterruptedException e) {
            System.out.println("Device discovery interrupted.");
        }
    }
}
