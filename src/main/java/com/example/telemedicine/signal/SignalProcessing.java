package com.example.telemedicine.signal;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.SignalType;
import uk.me.berndporr.iirj.Butterworth; // Tu librería de filtros

import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Utility class for parsing, converting and filtering biomedical signals.
 * Includes generic IIR filters, bandpass, notch filters and signal file parsing.
 */
public class SignalProcessing {

    /**
     * PARA EL SERVIDOR (Spring Boot):
     * Lee los bytes del archivo subido (MultipartFile).
     */
    /**
     * Parses a signal from uploaded bytes into a Signal object
     * @param fileBytes
     * @param type could be EMG or ECG
     * @param measurementSessionId
     * @return Parsed Signal object.
     */
    public static Signal parseSignalFile(byte[] fileBytes, SignalType type, Long measurementSessionId) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileBytes)))) {

            return parseFromReader(reader, type, measurementSessionId);
        } catch (IOException e) {
            throw new RuntimeException("Error reading uploaded signal file.", e);
        }
    }

    /**
     * Parses a local signal file into a Signal object.
     * @param filePath
     * @param type
     * @param measurementSessionId
     * @return Parsed Signal object.
     * @throws IOException If file reading process fails.
     */
    public static Signal parseLocalSignalFile(String filePath, SignalType type,
                                              Long measurementSessionId) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return parseFromReader(reader, type, measurementSessionId);
        }
    }


    private static Signal parseFromReader(BufferedReader reader, SignalType type, Long measurementSessionId) throws IOException {
        String firstLine = reader.readLine(); // Línea 1: Frecuencia
        String data = reader.readLine();      // Línea 2: Datos

        if (firstLine == null || firstLine.isBlank()) {
            throw new IllegalArgumentException("El archivo no tiene frecuencia (Línea 1 vacía).");
        }
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("El archivo no tiene datos (Línea 2 vacía).");
        }

        int fs;
        try {
            fs = Integer.parseInt(firstLine.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid sampling frequency in line 1.");
        }

        return new Signal(null, measurementSessionId, LocalDateTime.now(), type, data, fs);
    }

    // --- 3. CONVERSORES DE DATOS ---
    /**
     * Converts a comma-separated string into a double array.
     * @param dataString Comma-separated numeric string.
     * @return Array of doubles.
     */
    public static double[] stringToDoubleArray(String dataString) {
        if (dataString == null || dataString.isBlank()) return new double[0];
        return Arrays.stream(dataString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    // --- 4. FILTROS MATEMÁTICOS GENÉRICOS (IIRJ) ---

    /**
     * Converts the raw ADC signal into millivolts.
     * @param rawSignal
     * @param vcc Supply voltage.
     * @param resolution ADC resolution.
     * @param gain Amplifier gain.
     * @return Converted signal in millivolts.
     */
    public static double[] convertToMV(double[] rawSignal, double vcc, int resolution, int gain) {
        // Si tus datos ya vienen en mV o el formato es distinto, ajusta esto
        int bits = 10;
        return Arrays.stream(rawSignal)
                .map(value -> ((value / Math.pow(2, bits)) - 0.5) * vcc / gain * 1000)
                .toArray();
    }

    /**
     * Applies a bandpass IIR filter to the signal.
     * @param signal Input signal.
     * @param fs
     * @param lowcut Low cutoff frequency.
     * @param highcut High cutoff frequency.
     * @param order Filter order.
     * @return Filtered signal.
     */
    public static double[] bandpassFilter(double[] signal, double fs, double lowcut, double highcut, int order) {
        Butterworth butterworth = new Butterworth();
        double centerFrequency = (lowcut + highcut) / 2.0;
        double width = highcut - lowcut;
        butterworth.bandPass(order, fs, centerFrequency, width);

        double[] forwardFiltered = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            forwardFiltered[i] = butterworth.filter(signal[i]);
        }

        butterworth.reset();
        double[] backwardFiltered = new double[signal.length];
        for (int i = signal.length - 1; i >= 0; i--) {
            backwardFiltered[i] = butterworth.filter(forwardFiltered[i]);
        }
        return backwardFiltered;
    }

    /**
     * Applies a notch IIR filter to the signal.
     * @param signal Input signal.
     * @param fs
     * @param notchFreq
     * @param q Quality factor.
     * @return Filtered signal.
     */
    public static double[] notchFilter(double[] signal, double fs, double notchFreq, double q) {
        Butterworth butterworth = new Butterworth();
        double width = notchFreq / q;
        butterworth.bandStop(4, fs, notchFreq, width);

        double[] forwardFiltered = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            forwardFiltered[i] = butterworth.filter(signal[i]);
        }

        butterworth.reset();
        double[] backwardFiltered = new double[signal.length];
        for (int i = signal.length - 1; i >= 0; i--) {
            backwardFiltered[i] = butterworth.filter(forwardFiltered[i]);
        }
        return backwardFiltered;
    }

    /**
     * Converts a double array into a comma-separated string.
     * @param signal Input signal.
     * @return Comma-separated string representation of the signal.
     */
    public static String doubleArrayToString(double[] signal) {
        if (signal == null || signal.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < signal.length; i++) {
            sb.append(signal[i]);
            // Añadimos coma solo si no es el último elemento
            if (i < signal.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}

