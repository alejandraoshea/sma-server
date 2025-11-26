package com.example.telemedicine.signal;
import com.example.telemedicine.domain.Signal;
import com.example.telemedicine.domain.SignalType;
import uk.me.berndporr.iirj.Butterworth; // Tu librería de filtros

import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;


public class SignalProcessing {

    /**
     * PARA EL SERVIDOR (Spring Boot):
     * Lee los bytes del archivo subido (MultipartFile).
     */
    public static Signal parseSignalFile(byte[] fileBytes, SignalType type, Long measurementSessionId) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileBytes)))) {

            return parseFromReader(reader, type, measurementSessionId);
        } catch (IOException e) {
            throw new RuntimeException("Error reading uploaded signal file.", e);
        }
    }

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

    public static double[] stringToDoubleArray(String dataString) {
        if (dataString == null || dataString.isBlank()) return new double[0];
        return Arrays.stream(dataString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    // --- 4. FILTROS MATEMÁTICOS GENÉRICOS (IIRJ) ---
    public static double[] convertToMV(double[] rawSignal, double vcc, int resolution, int gain) {
        // Si tus datos ya vienen en mV o el formato es distinto, ajusta esto
        int bits = 10;
        return Arrays.stream(rawSignal)
                .map(value -> ((value / Math.pow(2, bits)) - 0.5) * vcc / gain * 1000)
                .toArray();
    }

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

