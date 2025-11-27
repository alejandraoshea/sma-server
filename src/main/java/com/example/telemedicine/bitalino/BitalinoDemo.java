package com.example.telemedicine.bitalino;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BitalinoDemo {

    public static Frame[] frame;

    public static void main(String[] args) {
        BITalino bitalino = null;
        try {
            bitalino = new BITalino();

            // 1) Discover BITalino serial ports (after pairing in macOS Bluetooth settings). Check the pairing and if
            // it does not work, unpair and pair it again.
            Vector<String> ports = bitalino.findDevices();
            System.out.println("Discovered BITalino ports: " + ports);

            // You can pass the port as the first CLI arg, e.g.:
            //  java ceu.biolab.BitalinoDemoSerial /dev/cu.BITalino-XX-XX-DevB
            String portToUse = (args.length > 0) ? args[0]
                    : (ports.isEmpty() ? null : ports.firstElement());

            if (portToUse == null) {
                System.err.println("No BITalino serial port found. Pair the device and try again.");
                return;
            }
            System.out.println("Using port: " + portToUse);

            // 2) Open the device (sampling rate: 10, 100, or 1000 Hz)
            int samplingRate = 10;
            bitalino.open(portToUse, samplingRate);

            // 3) Start acquisition on analog channels A2 and A6 (i.e., indices 1 and 5)
            int[] channelsToAcquire = {1, 5};
            bitalino.start(channelsToAcquire);

            // 4) Read blocks of samples and print them. Check the variables
            final int blockSize = 10;
            final int numBlocks = 10000000;
            for (int j = 0; j < numBlocks; j++) {
                frame = bitalino.read(blockSize);
                System.out.println("size block: " + frame.length);
                for (int i = 0; i < frame.length; i++) {
                    System.out.println((j * blockSize + i) + " seq: " + frame[i].seq + " "
                            + frame[i].analog[0] + " "
                            + frame[i].analog[1]);
                }
            }

            // 5) Stop acquisition
            bitalino.stop();

        } catch (BITalinoException ex) {
            Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Throwable ex) {
            Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (bitalino != null) {
                    bitalino.close();
                }
            } catch (BITalinoException ex) {
                Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

