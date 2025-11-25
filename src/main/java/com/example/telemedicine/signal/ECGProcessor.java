package com.example.telemedicine.signal;

import com.example.telemedicine.signal.SignalUtils;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ECGProcessor {

    // --- CLASE DE RESULTADOS ---
    public static class QRSResult {
        public final List<Integer> rPeaks;
        public final List<Integer> qPeaks;

        public QRSResult(List<Integer> r, List<Integer> q) {
            this.rPeaks = r;
            this.qPeaks = q;
        }
    }

    // --- MÉTODOS ESPECÍFICOS DE ECG ---

    /**
     * Aplica filtros específicos para ECG (Pasa-banda 5-15Hz y Notch 60Hz).
     */
    public static double[] applyFilters(double[] signal, double fs) {
        // Llama a la caja de herramientas (SignalUtils)
        double[] passFiltered = SignalUtils.bandpassFilter(signal, fs, 5.0, 15.0, 4);
        return SignalUtils.notchFilter(passFiltered, fs, 60.0, 30);
    }

    /**
     * Lógica principal de detección de QRS (Sección 5 de tu práctica).
     */
    public static QRSResult detectQRSComplexes(double[] filteredSignal, double fs) {
        // 1. Detectar Picos R (los altos)
        double maxAmplitude = Arrays.stream(filteredSignal).max().orElse(0);
        double rPeakThreshold = 0.5 * maxAmplitude; // Umbral al 50%

        List<Integer> rPeaks = findPeaks(filteredSignal, rPeakThreshold);

        // 2. Detectar Picos Q (el mínimo local anterior a cada R)
        List<Integer> qPeaks = new ArrayList<>();
        int windowSize = (int) (fs * 0.05); // Ventana de 50 ms

        for (int rPeakIndex : rPeaks) {
            int start = Math.max(0, rPeakIndex - windowSize);
            int end = rPeakIndex;

            if (start >= end) continue;

            // Buscar el mínimo en la ventana anterior al pico R
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

    // Método auxiliar privado para encontrar picos locales
    private static List<Integer> findPeaks(double[] signal, double height) {
        List<Integer> peaks = new ArrayList<>();
        for (int i = 1; i < signal.length - 1; i++) {
            if (signal[i] > signal[i - 1] && signal[i] >= signal[i + 1] && signal[i] > height) {
                peaks.add(i);
            }
        }
        return peaks;
    }

    // --- GRÁFICO (Estilo corregido) ---
    public static void plotECGResults(double[] time, double[] signal, List<Integer> qPeaks, String title, double maxTimeSec) {
        double fs = 1.0 / (time[1] - time[0]);
        int maxSamples = (int) (maxTimeSec * fs);
        int limit = Math.min(signal.length, maxSamples);

        double actualMaxTime = time[limit - 1];
        double[] timeLimited = Arrays.copyOfRange(time, 0, limit);
        double[] signalLimited = Arrays.copyOfRange(signal, 0, limit);

        List<Double> qPeakTimes = new ArrayList<>();
        List<Double> qPeakAmplitudes = new ArrayList<>();
        for (int peakIndex : qPeaks) {
            if (peakIndex < limit) {
                qPeakTimes.add(time[peakIndex]);
                qPeakAmplitudes.add(signal[peakIndex]);
            }
        }

        XYChart chart = new XYChartBuilder().width(1200).height(600).title(title).xAxisTitle("Time (s)").yAxisTitle("Amplitude (mV)").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);

        // Señal Roja Limpia
        XYSeries signalSeries = chart.addSeries("Señal Procesada", timeLimited, signalLimited);
        signalSeries.setLineColor(Color.RED);
        signalSeries.setMarker(SeriesMarkers.NONE);

        // Picos Q Negros (Sin línea que los una)
        XYSeries peakSeries = chart.addSeries("Picos Q Detectados", qPeakTimes, qPeakAmplitudes);
        peakSeries.setMarker(SeriesMarkers.CIRCLE);
        peakSeries.setMarkerColor(Color.BLACK);

        chart.getStyler().setXAxisMin(0.0);
        chart.getStyler().setXAxisMax(actualMaxTime);

        new SwingWrapper<>(chart).displayChart();
    }

    // --- MAIN (Simulación Local del Servidor) ---
    public static void main(String[] args) {
        try {
            // 1. CONFIGURACIÓN
            // ¡Asegúrate de usar el archivo generado por FileConverter!
            String filePath = "C:/Users/phora/OneDrive/Documentos/Uni_Ceu/Tercer año/Segundo Cuatri/Señales Biomédicas/Prácticas/Pablo_Morales/ecg_SERVER_READY.txt";
            double maxPlotTimeSec = 10.0; // Visualizar 10 segundos

            System.out.println("--- INICIO SIMULACIÓN SERVIDOR (ECG) ---");

            // 2. LEER ARCHIVO (Usando SignalUtils)
            System.out.println("1. Leyendo archivo...");
            SignalUtils.PatientSignals info = SignalUtils.readAndParseFile(filePath);

            System.out.println("   -> Frecuencia: " + info.fs + " Hz");

            // 3. CONVERTIR TEXTO A NÚMEROS
            double[] rawECG = SignalUtils.stringToDoubleArray(info.dataString);
            System.out.println("   -> Muestras: " + rawECG.length);

            // 4. PREPARAR SEÑAL (Conversión a mV)
            double[] ecgSignal = new double[rawECG.length];
            for(int i=0; i<rawECG.length; i++) {
                ecgSignal[i] = rawECG[i] / 1000.0; // Conversión típica ADC->mV
            }

            // 5. FILTRAR
            System.out.println("2. Filtrando señal...");
            double[] filteredECG = applyFilters(ecgSignal, (double) info.fs);

            // 6. DETECTAR
            System.out.println("3. Detectando complejos QRS...");
            QRSResult qrs = detectQRSComplexes(filteredECG, (double) info.fs);

            System.out.println("Proceso completado.");
            System.out.println("Picos Q detectados: " + qrs.qPeaks.size());

            // 7. GRAFICAR
            double[] time = new double[filteredECG.length];
            for (int i = 0; i < time.length; i++) {
                time[i] = i / (double) info.fs;
            }

            System.out.println("Generando gráfico...");
            plotECGResults(time, filteredECG, qrs.qPeaks, "Análisis ECG (Simulación Servidor)", maxPlotTimeSec);

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
