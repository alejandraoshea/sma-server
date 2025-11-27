/**
 * \mainpage
 * <p>
 * The %BITalino Java API (available at http://www.bitalino.com/API/API_Java.zip) is a BlueCove-powered library which enables Java applications to communicate with a %BITalino device through a simple interface.
 * The API is composed of implementation files (BITalino.java and Frame.java), a set of auxiliary files to handle errors and exception (BITalinoErrorTypes.java and BITalinoException.java), and an auxiliary file for device discovery (DeviceDiscoverer.java).
 * A sample test application in Java (test.java()) is also provided.
 * <p>
 * This code base has been designed to enable direct Bluetooth connection using the device Bluetooth MAC address (Windows and Mac OS);
 * <p>
 * The API exposes the class BITalino, and each instance of this class represents a connection to a %BITalino device. The connection is established with the BITalino.open(...) method and released with the BITalino.close() method.
 * <p>
 * \section sampleapp About the sample application
 * <p>
 * The sample application (test.java) creates an instance to a %BITalino device.
 * Then it opens the connection, starts acquiring channels 1 and 5 on the device at 1000 Hz, reads 300 samples and toggles the digital outputs (green LED should turn on). Afterwards, the acquisition is stopped and the connection closed.
 * <p>
 * The BITalino.open() method must be used to connect to the device.
 * The string passed to the constructor should be a Bluetooth MAC address including the ':' delimiter (to use the sample application you must change the MAC address therein).
 * <p>
 * \section configuration Configuring the IDE
 * <p>
 * To use the library and sample application:
 * - launch your IDE;
 * - make sure that you have the Eclipse Integration Plugin installed (if you haven't, go to "File > Settings > Plugins > Install JetBrains Plugin…" and install the plugin);
 * - to import the Java API project go to "File > New… > From Existing Sources…", then select your project folder in the dialog window, choose the .project file and press "OK";
 * - make sure that the “Select Eclipse projects directory:” field is the actual path of your project folder and then press "Next";
 * - if unselected, select the project “API_BITalino” in order to import it and press "Next";
 * - select your project SDK and press "Finish";
 * - as the file .userlibraries is not available in the project, when the IDE asks for it just press "Cancel";
 * - now that the project has been successfully imported, go to "File > Project Structure…", under "Project Settings", select "Modules" and click on API_BITALINO;
 * - at this point, in the "Dependencies" tab, make sure that the Module SDK is the same that you choose to your project;
 * - then remove the “Referenced Libraries” by selecting and pressing the minus icon on the left of the window;
 * - press "Apply" and then "OK" in the bottom of the dialog;
 * - under the API_BITALINO folder, select the "src" folder.
 * - click on the test.java file with the right button of your mouse and select the “Run ‘test.main()’”.
 */

package com.example.telemedicine.bitalino;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.regex.Pattern;

/// The %BITalino device class (serial-port version; no BlueCove required).
public class BITalino {

    /// Array with the list of analog inputs to be acquired from the device (auxiliary variable)
    private int[] analogChannels;

    /// Number of bytes expected for a frame sent by the device (auxiliary variable)
    private int number_bytes;

    /// Serial port handle (replaces StreamConnection)
    private SerialPort serialPort;

    /// Data streams (replace DataInputStream/DataOutputStream)
    private InputStream iStream;
    private OutputStream oStream;

    private static final Pattern BITALINO_TTY = Pattern.compile("^(\\/dev\\/)?(tty|cu)\\.BITalino.*");

    public BITalino() {
        number_bytes = 0;
        analogChannels = null;
        serialPort = null;
        iStream = null;
        oStream = null;
    }

    /**
     * Searches available serial ports and returns those that look like BITalino.
     * NOTE: Return type Vector<String> with POSIX port paths.
     *
     * @return a vector of names of the tty serial connections like bitalino
     */
    public Vector<String> findDevices() throws InterruptedException {
        Vector<String> ports = new Vector<>();
        for (SerialPort p : SerialPort.getCommPorts()) {
            String sys = p.getSystemPortName();       // e.g., /dev/cu.BITalino-XX-XX-DevB or tty.BITalino-XX-XX
            String desc = p.getDescriptivePortName(); // may include "BITalino"
            boolean looksLikeBitalino =
                    (sys != null && BITALINO_TTY.matcher(sys).matches()) &&
                            (desc != null && desc.toLowerCase().contains("bitalino"));
            if (looksLikeBitalino) {
                ports.add(sys != null ? sys : desc);
            }
        }
        return ports;
    }

    /**
     * Default sampling rate to 100 to open the connection to Bitalino
     *
     * @param arg
     * @throws Throwable
     */
    public void open(String arg) throws Throwable {
        open(arg, 100);
    }

    /**
     * Opens a serial connection to BITalino at the requested sampling rate.
     * On macOS, pass the serial device path (e.g., /dev/cu.BITalino-XX-XX-DevB).
     * For backward compatibility, if a MAC string is provided, we pick the first discovered BITalino port.
     * If no devide is found in arg, it tries to discover the devices and use the first one listed as bitalino
     * in serial communications.
     *
     * @throws BITalinoException if arg is null and it does not find any device.
     */
    public void open(String arg, int samplingRate) throws BITalinoException {
        // Decide whether arg is a path or a legacy MAC-like string
        String portPath;
        if (arg != null && arg.contains("/")) {
            portPath = arg;
        } else {
            // legacy MAC flow: we cannot map MAC->port on macOS; choose the first matching port
            Vector<String> candidates;
            try {
                candidates = this.findDevices();
            } catch (InterruptedException e) {
                throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
            }
            if (candidates.isEmpty()) {
                throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
            }
            portPath = candidates.firstElement();
        }

        try {
            serialPort = SerialPort.getCommPort(portPath);
            serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

            if (!serialPort.openPort()) {
                throw new IOException("Failed to open port: " + portPath);
            }
            iStream = serialPort.getInputStream();
            oStream = serialPort.getOutputStream();

            // Allow device to settle, similar to your original 2s pause
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }

        } catch (Exception e) {
            try {
                close();
            } catch (Exception ignored) {
            }
            throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
        }

        // Configure sampling rate (same mapping as original)
        try {
            int variableToSend;
            switch (samplingRate) {
                case 1000:
                    variableToSend = 0x3;
                    break;
                case 100:
                    variableToSend = 0x2;
                    break;
                case 10:
                    variableToSend = 0x1;
                    break;
                case 1:
                    variableToSend = 0x0;
                    break;
                default:
                    close();
                    throw new BITalinoException(BITalinoErrorTypes.SAMPLING_RATE_NOT_DEFINED);
            }
            variableToSend = (variableToSend << 6) | 0x03;
            this.write(variableToSend);
        } catch (BITalinoException e) {
            throw e;
        } catch (Exception e) {
            throw new BITalinoException(BITalinoErrorTypes.SAMPLING_RATE_NOT_DEFINED);
        }
    }

    /**
     * @param anChannels channels to capture from 0 to 5 since bitalino only has 6 analogic inputs
     * @throws Throwable
     */
    public void start(int[] anChannels) throws Throwable {
        analogChannels = anChannels;
        if (analogChannels.length > 6 | analogChannels.length == 0) {
            throw new BITalinoException(BITalinoErrorTypes.ANALOG_CHANNELS_NOT_VALID);
        } else {
            int bit = 1;
            for (int i : anChannels) {
                if (i < 0 | i > 5) {
                    throw new BITalinoException(BITalinoErrorTypes.ANALOG_CHANNELS_NOT_VALID);
                } else {
                    bit = bit | 1 << (2 + i);
                }
            }
            int nChannels = analogChannels.length;
            if (nChannels <= 4) {
                number_bytes = (int) Math.ceil(((float) 12 + (float) 10 * nChannels) / 8);
            } else {
                number_bytes = (int) Math.ceil(((float) 52 + (float) 6 * (nChannels - 4)) / 8);
            }
            try {
                this.write(bit);
            } catch (Exception e) {
                throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
            }
        }
    }

    /**
     * @throws BITalinoException Write a 0 to send the command to stop the connection
     */
    public void stop() throws BITalinoException {
        try {
            this.write(0);
        } catch (Exception e) {
            throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
        }
    }

    public void close() throws BITalinoException {
        try {
            if (oStream != null) {
                try {
                    oStream.close();
                } catch (Exception ignored) {
                }
            }
            if (iStream != null) {
                try {
                    iStream.close();
                } catch (Exception ignored) {
                }
            }
            if (serialPort != null && serialPort.isOpen()) {
                serialPort.closePort();
            }
            serialPort = null;
            iStream = null;
            oStream = null;
        } catch (Exception e) {
            throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
        }
    }

    public void write(int data) throws BITalinoException {
        try {
            if (oStream == null) throw new IOException("port not open");
            oStream.write(data);
            oStream.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        } catch (Exception e) {
            throw new BITalinoException(BITalinoErrorTypes.LOST_COMMUNICATION);
        }
    }

    public void battery(int value) throws BITalinoException {
        int Mode;
        if (value >= 0 && value <= 63) {
            Mode = value << 2;
            this.write(Mode);
        } else {
            throw new BITalinoException(BITalinoErrorTypes.THRESHOLD_NOT_VALID);
        }
    }

    public void trigger(int[] digitalArray) throws BITalinoException {
        if (digitalArray.length != 4) {
            throw new BITalinoException(BITalinoErrorTypes.DIGITAL_CHANNELS_NOT_VALID);
        } else {
            int data = 3;
            for (int i = 0; i < digitalArray.length; i++) {
                if (digitalArray[i] < 0 | digitalArray[i] > 1) {
                    throw new BITalinoException(BITalinoErrorTypes.DIGITAL_CHANNELS_NOT_VALID);
                } else {
                    data = data | digitalArray[i] << (2 + i);
                }
            }
            this.write(data);
        }
    }

    public String version() throws BITalinoException, IOException {
        try {
            this.write(7);
            byte[] version = new byte[30];
            String test;
            int i = 0;
            while (true) {
                int r = iStream.read(version, i, 1);
                if (r < 0) throw new IOException("EOF");
                i++;
                test = new String(new byte[]{version[i - 1]});
                if (test.equals("\n")) {
                    break;
                }
                if (i >= version.length) break; // safety
            }
            return new String(version, 0, Math.max(i - 1, 0));
        } catch (Exception e) {
            throw new BITalinoException(BITalinoErrorTypes.LOST_COMMUNICATION);
        }
    }

    private Frame[] decode(byte[] buffer) throws IOException, BITalinoException {
        try {
            Frame[] frames = new Frame[1];
            int j = (number_bytes - 1), i = 0, CRC = 0, x0 = 0, x1 = 0, x2 = 0, x3 = 0, out = 0, inp = 0;
            CRC = (buffer[j - 0] & 0x0F) & 0xFF;
            // check CRC
            for (int bytes = 0; bytes < number_bytes; bytes++) {
                for (int bit = 7; bit > -1; bit--) {
                    inp = (buffer[bytes]) >> bit & 0x01;
                    if (bytes == (number_bytes - 1) && bit < 4) {
                        inp = 0;
                    }
                    out = x3;
                    x3 = x2;
                    x2 = x1;
                    x1 = out ^ x0;
                    x0 = inp ^ out;
                }
            }
            // if the message was correctly received, decode
            if (CRC == ((x3 << 3) | (x2 << 2) | (x1 << 1) | x0)) {
                frames[i] = new Frame();
                frames[i].seq = (short) ((buffer[j - 0] & 0xF0) >> 4) & 0xf;
                frames[i].digital[0] = (short) ((buffer[j - 1] >> 7) & 0x01);
                frames[i].digital[1] = (short) ((buffer[j - 1] >> 6) & 0x01);
                frames[i].digital[2] = (short) ((buffer[j - 1] >> 5) & 0x01);
                frames[i].digital[3] = (short) ((buffer[j - 1] >> 4) & 0x01);

                switch (analogChannels.length - 1) {
                    case 5:
                        frames[i].analog[5] = (short) ((buffer[j - 7] & 0x3F));
                    case 4:
                        frames[i].analog[4] = (short) ((((buffer[j - 6] & 0x0F) << 2) | ((buffer[j - 7] & 0xc0) >> 6)) & 0x3f);
                    case 3:
                        frames[i].analog[3] = (short) ((((buffer[j - 5] & 0x3F) << 4) | ((buffer[j - 6] & 0xf0) >> 4)) & 0x3ff);
                    case 2:
                        frames[i].analog[2] = (short) ((((buffer[j - 4] & 0xff) << 2) | (((buffer[j - 5] & 0xc0) >> 6))) & 0x3ff);
                    case 1:
                        frames[i].analog[1] = (short) ((((buffer[j - 2] & 0x3) << 8) | (buffer[j - 3]) & 0xff) & 0x3ff);
                    case 0:
                        frames[i].analog[0] = (short) ((((buffer[j - 1] & 0xF) << 6) | ((buffer[j - 2] & 0XFC) >> 2)) & 0x3ff);
                }
            } else {
                frames[i] = new Frame();
                frames[i].seq = -1;
            }
            return frames;
        } catch (Exception e) {
            throw new BITalinoException(BITalinoErrorTypes.INCORRECT_DECODE);
        }
    }

    public Frame[] read(int nSamples) throws BITalinoException {
        try {
            Frame[] frames = new Frame[nSamples];
            byte[] buffer = new byte[number_bytes];
            byte[] bTemp = new byte[1];
            int i = 0;
            while (i < nSamples) {
                int read = 0;
                while (read < number_bytes) {
                    int r = iStream.read(buffer, read, number_bytes - read);
                    if (r < 0) throw new IOException("EOF");
                    read += r;
                }
                Frame[] f = decode(buffer);
                if (f[0].seq == -1) {
                    while (f[0].seq == -1) {
                        int r = iStream.read(bTemp, 0, 1);
                        if (r < 0) throw new IOException("EOF");
                        for (int j = number_bytes - 2; j >= 0; j--) {
                            buffer[j + 1] = buffer[j];
                        }
                        buffer[0] = bTemp[0];
                        f = decode(buffer);
                    }
                    frames[i] = f[0];
                } else {
                    frames[i] = f[0];
                }
                i++;
            }
            return frames;
        } catch (Exception e) {
            throw new BITalinoException(BITalinoErrorTypes.LOST_COMMUNICATION);
        }
    }
}
