package com.example.telemedicine.signal;

// Importaciones de XChart para graficar
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.None;
import org.knowm.xchart.style.markers.SeriesMarkers;

// Importaciones estándar de Java
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.awt.Color.RED;
import static java.awt.Transparency.TRANSLUCENT;

/**
 * Procesa una señal de ECG para detectar complejos QRS.
 * Utiliza SignalUtils para las operaciones de filtrado genéricas.
 */
public class ECGProcessor {

    // --- CLASE INTERNA PARA RESULTADOS ---
    public static class QRSResult {
        public final List<Integer> rPeaks;
        public final List<Integer> qPeaks;

        public QRSResult(List<Integer> rPeaks, List<Integer> qPeaks) {
            this.rPeaks = rPeaks;
            this.qPeaks = qPeaks;
        }
    }

    // --- MÉTODOS ESPECÍFICOS DE ECG ---

    /**
     * Carga la señal de ECG desde la columna 1 y la convierte a mV.
     */
    public static double[] loadECGData(String filePath) throws IOException {
        // Llama a SignalUtils para cargar la columna 1
        double[] rawSignal = SignalUtils.loadSignal(filePath, 1);

        // La conversión es específica de ECG (microV a mV)
        for (int i = 0; i < rawSignal.length; i++) {
            rawSignal[i] = rawSignal[i] / 1000.0;
        }
        return rawSignal;
    }

    /**
     * Aplica los filtros Pasa-Banda (5-15 Hz) y Notch (60 Hz).
     */
    public static double[] applyFilters(double[] signal, double fs) {
        System.out.println("Aplicando filtro pasa-banda (5-15 Hz)...");
        // Llama a SignalUtils
        double[] passFiltered = SignalUtils.bandpassFilter(signal, fs, 5.0, 15.0, 4);

        System.out.println("Aplicando filtro notch (60 Hz)...");
        // Llama a SignalUtils
        double[] notchFiltered = SignalUtils.notchFilter(passFiltered, fs, 60.0, 30);

        return notchFiltered;
    }

    /**
     * Encuentra todos los máximos locales en una señal que están por encima de un umbral.
     * (Esta es una función de utilidad interna, específica para la detección de R-peaks).
     */
    private static List<Integer> findPeaks(double[] signal, double height) {
        List<Integer> peaks = new ArrayList<>();
        // Itera desde la segunda hasta la penúltima muestra
        for (int i = 1; i < signal.length - 1; i++) {
            // Comprueba si es un máximo local (más alto que sus vecinos)
            boolean isLocalMaximum = signal[i] > signal[i - 1] && signal[i] >= signal[i + 1];
            // Comprueba si supera el umbral
            if (isLocalMaximum && signal[i] > height) {
                peaks.add(i);
            }
        }
        return peaks;
    }

    /**
     * Detecta los picos R y Q de la señal filtrada (lógica de la Sección 5).
     */
    public static QRSResult detectQRSComplexes(double[] filteredSignal, double fs) {
        System.out.println("Detectando picos R...");

        // 1. Encontrar picos R
        double maxAmplitude = Arrays.stream(filteredSignal).max().orElse(0);
        double rPeakThreshold = 0.5 * maxAmplitude; // Umbral del 50% del máximo

        // Llama al método findPeaks local (privado)
        List<Integer> rPeaks = findPeaks(filteredSignal, rPeakThreshold);

        System.out.println("Detectando picos Q...");
        List<Integer> qPeaks = new ArrayList<>();

        // 2. Buscar picos Q (mínimos) en una ventana antes de cada pico R
        int windowSize = (int) (fs * 0.05); // Ventana de 50 ms

        for (int rPeakIndex : rPeaks) {
            int start = Math.max(0, rPeakIndex - windowSize);
            int end = rPeakIndex;

            if (start >= end) continue; // Si la ventana es inválida

            // Buscar el índice del valor mínimo en la ventana
            double minVal = filteredSignal[start];
            int qPeakIndexInWindow = 0;
            for (int i = 1; i < (end - start); i++) {
                if (filteredSignal[start + i] < minVal) {
                    minVal = filteredSignal[start + i];
                    qPeakIndexInWindow = i;
                }
            }
            qPeaks.add(start + qPeakIndexInWindow);
        }

        return new QRSResult(rPeaks, qPeaks);
    }

    /**
     * Grafica los resultados ESPECÍFICOS de ECG (señal y picos Q).
     */
    public static void plotECGResults(double[] time, double[] signal, List<Integer> qPeaks, String title, double maxTimeSec) {
        // Calcula la frecuencia de muestreo
        double fs = 1.0 / (time[1] - time[0]);

        // Calcula el número máximo de muestras a trazar
        int maxSamples = (int) (maxTimeSec * fs);

        // El límite real es el valor más pequeño entre la longitud de la señal y el máximo deseado
        int limit = Math.min(signal.length, maxSamples);

        // Comprueba si la señal es más corta que el máximo solicitado
        double actualMaxTime = time[limit - 1];
        if (limit < maxSamples) {
            System.out.println("La señal es más corta que " + maxTimeSec + " segundos. Mostrando señal completa: " + String.format("%.2f", actualMaxTime) + "s");
        }
        double[] timeLimited = Arrays.copyOfRange(time, 0, limit);
        double[] signalLimited = Arrays.copyOfRange(signal, 0, limit);

        // Preparar los datos de los picos
        List<Double> qPeakTimes = new ArrayList<>();
        List<Double> qPeakAmplitudes = new ArrayList<>();
        for (int peakIndex : qPeaks) {
            if (peakIndex < limit) {
                qPeakTimes.add(time[peakIndex]);
                qPeakAmplitudes.add(signal[peakIndex]);
            }
        }

        // Crear el gráfico
        XYChart chart = new XYChart(1200, 600);
        chart.setTitle("sECG with Detected QRS Complex ");
        chart.setXAxisTitle("Time (s)");
        chart.setYAxisTitle("Amplitude (mV)");
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setPlotGridLinesVisible(true);
        // Añadir la señal de ECG
        chart.addSeries("Señal Procesada", timeLimited, signalLimited).setMarker(SeriesMarkers.NONE);

        // Añadir los picos Q como puntos
        XYSeries peakSeries = chart.addSeries("Picos Q Detectados", qPeakTimes, qPeakAmplitudes);
        peakSeries.setLineColor(Color.lightGray);
        peakSeries.setMarker(SeriesMarkers.CIRCLE);
        peakSeries.setMarkerColor(RED);

        chart.getStyler().setXAxisMin(0.0);
        chart.getStyler().setXAxisMax(actualMaxTime);

        new SwingWrapper<>(chart).displayChart();
    }


    /**
     * Función principal para ejecutar todo el proceso de ECG.
     */
    public static void main(String[] args) {
        try {
            // --- Configuración ---
            String filePath = "C:/Users/phora/OneDrive/Documentos/Uni_Ceu/Tercer año/Segundo Cuatri/Señales Biomédicas/Práctica 3/ecg_Practice3.txt"; // Ruta

            double fs = 300.0;

            // --- 1. Cargar Datos ---
            System.out.println("Cargando datos de ECG...");
            double[] ecgSignal = loadECGData(filePath);

            // --- 2. Filtrar ---
            System.out.println("Filtrando señal de ECG...");
            double[] filteredECG = applyFilters(ecgSignal, fs);

            // --- 3. Detectar Picos ---
            System.out.println("Detectando complejos QRS...");
            QRSResult qrs = detectQRSComplexes(filteredECG, fs);

            System.out.println("Proceso completado.");
            System.out.println("Picos R detectados: " + qrs.rPeaks.size());
            System.out.println("Picos Q detectados: " + qrs.qPeaks.size());

            // --- 4. Graficar ---
            // Preparar el eje de tiempo para el gráfico
            double[] time = new double[filteredECG.length];
            for (int i = 0; i < time.length; i++) {
                time[i] = i / fs;
            }

            System.out.println("Generando gráfico de ECG...");
            plotECGResults(time, filteredECG, qrs.qPeaks, "Detección de Picos Q en ECG (Primeros 10 seg)", 180);

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ha ocurrido un error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
