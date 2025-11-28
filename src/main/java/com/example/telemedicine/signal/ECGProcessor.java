package com.example.telemedicine.signal;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides processing ECG signal methods, including filtering.
 * QRS complex detection and plotting results.
 */
public class ECGProcessor {

    /**
     * Aplica filtros específicos para ECG (Pasa-banda 5-15Hz y Notch 60Hz).
     */
    /**
     * Applies bandpass (5-15 Hz) and notch (60 Hz) filters to an ECG signal.
     * @param signal Input recovered ECG signal.
     * @param fs Sampling frequency
     * @return Filtered ECG signal.
     */
    public static double[] applyFilters(double[] signal, double fs) {
        // Llama a la caja de herramientas (SignalUtils)
        double[] passFiltered = SignalProcessing.bandpassFilter(signal, fs, 5.0, 15.0, 4);
        return SignalProcessing.notchFilter(passFiltered, fs, 60.0, 30);
    }

    /**
     * Lógica principal de detección de QRS (Sección 5 de tu práctica).
     */
    /**
     * Detects QRS complexes in a filtered ECG signal.
     * R-peaks are detected based on the threshold and Q-peaks on the minima
     * preceding each R-peak.
     * @param filteredSignal
     * @param fs
     * @return signal containing indices of R-peaks and Q-peaks.
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
    //Private helper method for peak detection
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

    /**
     * Plots ECG signal and detected Q-peaks.
     * @param time
     * @param signal ECG signal to plot.
     * @param qPeaks Indices of detected Q-peaks.
     * @param title Chart title.
     * @param maxTimeSec Maximum time (seconds) to display in the plot.
     */
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
}
