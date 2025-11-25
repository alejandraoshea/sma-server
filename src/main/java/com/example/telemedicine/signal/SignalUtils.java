package com.example.telemedicine.signal;
import uk.me.berndporr.iirj.Butterworth; // Tu librería de filtros

import java.io.*;
import java.util.Arrays;

/**
 * CAJA DE HERRAMIENTAS (Utilidades Estáticas)
 * Responsabilidad: Parsear archivos, convertir formatos y aplicar matemáticas (filtros).
 * No accede a Base de Datos.
 */
public class SignalUtils {

    // --- 1. CLASE AUXILIAR PARA TRANSPORTAR DATOS ---
    public static class PatientSignals {
        public final int fs;
        public final String dataString;

        public PatientSignals(int fs, String dataString) {
            this.fs = fs;
            this.dataString = dataString;
        }


    }

    // --- 2. MÉTODOS DE LECTURA DE ARCHIVOS ---

    /**
     * PARA EL SERVIDOR (Spring Boot):
     * Lee los bytes del archivo subido (MultipartFile).
     */
    public static PatientSignals parseSignalFile(byte[] fileBytes) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileBytes)))) {
            return readFromReader(reader);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer los bytes del archivo.", e);
        }
    }

    /**
     * PARA PRUEBAS LOCALES:
     * Lee un archivo del disco duro.
     */
    public static PatientSignals readAndParseFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return readFromReader(reader);
        }
    }

    private static PatientSignals readFromReader(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine(); // Línea 1: Frecuencia
        String data = reader.readLine();      // Línea 2: Datos

        if (firstLine == null || firstLine.isBlank()) {
            throw new IllegalArgumentException("El archivo no tiene frecuencia (Línea 1 vacía).");
        }
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("El archivo no tiene datos (Línea 2 vacía).");
        }

        try {
            int fs = Integer.parseInt(firstLine.trim());
            return new PatientSignals(fs, data);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La primera línea no es un número válido (Frecuencia).");
        }
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
}
