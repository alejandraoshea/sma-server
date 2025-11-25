package com.example.telemedicine.signal;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Procesa una señal de EMG para detectar contracciones musculares.
 * Utiliza SignalUtils para las operaciones de filtrado genéricas.
 */
public class EMGProcessor {

    // --- CLASES DE RESULTADOS ---
    public static class ContractionResult {
        public final List<Integer> onsets;
        public final List<Integer> offsets;
        public final double[] envelope;

        public ContractionResult(List<Integer> onsets, List<Integer> offsets, double[] envelope) {
            this.onsets = onsets;
            this.offsets = offsets;
            this.envelope = envelope;
        }
    }

    public static class RmsResult {
        public final List<Double> rmsValues;
        public final double medianRms;

        public RmsResult(List<Double> rmsValues, double medianRms) {
            this.rmsValues = rmsValues;
            this.medianRms = medianRms;
        }
    }

    // --- MÉTODOS PRIVADOS DE PROCESAMIENTO (Exclusivos de EMG) ---

    /**
     * Filtro de Mediana para suavizar la envolvente.
     * (Movido desde SignalUtils porque es específico de EMG)
     */
    private static double[] medianFilter(double[] signal, int kernelSize) {
        double[] result = new double[signal.length];
        int halfKernel = kernelSize / 2;

        for (int i = 0; i < signal.length; i++) {
            int start = Math.max(0, i - halfKernel);
            int end = Math.min(signal.length, i + halfKernel + 1);

            double[] window = Arrays.copyOfRange(signal, start, end);
            Arrays.sort(window);

            if (window.length % 2 == 1) {
                result[i] = window[window.length / 2];
            } else {
                result[i] = (window[window.length / 2 - 1] + window[window.length / 2]) / 2.0;
            }
        }
        return result;
    }

    private static double[] computeHilbertEnvelope(double[] signal) {
        int originalLength = signal.length;
        int nextPowerOf2 = (int) Math.pow(2, Math.ceil(Math.log(originalLength) / Math.log(2)));

        double[] paddedSignal = new double[nextPowerOf2];
        System.arraycopy(signal, 0, paddedSignal, 0, originalLength);

        FastFourierTransformer fft = new FastFourierTransformer(org.apache.commons.math3.transform.DftNormalization.STANDARD);
        Complex[] complexSignal = Arrays.stream(paddedSignal).mapToObj(d -> new Complex(d, 0)).toArray(Complex[]::new);
        Complex[] analyticSignal = fft.transform(complexSignal, TransformType.FORWARD);

        for (int i = 1; i < (analyticSignal.length + 1) / 2; i++) {
            analyticSignal[i] = analyticSignal[i].multiply(2);
        }
        for (int i = (analyticSignal.length + 1) / 2; i < analyticSignal.length; i++) {
            analyticSignal[i] = Complex.ZERO;
        }

        analyticSignal = fft.transform(analyticSignal, TransformType.INVERSE);
        double[] paddedEnvelope = Arrays.stream(analyticSignal).mapToDouble(Complex::abs).toArray();

        return Arrays.copyOfRange(paddedEnvelope, 0, originalLength);
    }

    // --- LÓGICA DE DETECCIÓN ---

    public static double[] convertToMV(double[] rawSignal, double vcc, int resolution, int gain) {
        // Si tus datos ya vienen en mV o el formato es distinto, ajusta esto
        int bits = 10;
        return Arrays.stream(rawSignal)
                .map(value -> ((value / Math.pow(2, bits)) - 0.5) * vcc / gain * 1000)
                .toArray();
    }

    public static ContractionResult detectContractions(double[] signal, double fs, double thresholdRatio, double minDurationSec) {
        System.out.println("Calculando envolvente de Hilbert...");
        double[] envelope = computeHilbertEnvelope(signal);

        System.out.println("Suavizando envolvente...");
        // Llamada al método privado local
        double[] envelopeSmooth = medianFilter(envelope, 201);

        double maxEnvelope = Arrays.stream(envelopeSmooth).max().orElse(0);
        double threshold = thresholdRatio * maxEnvelope;

        boolean[] aboveThreshold = new boolean[envelopeSmooth.length];
        for (int i = 0; i < envelopeSmooth.length; i++) {
            aboveThreshold[i] = envelopeSmooth[i] > threshold;
        }

        List<Integer> onsets = new ArrayList<>();
        List<Integer> offsets = new ArrayList<>();
        boolean isActive = false;
        for (int i = 0; i < aboveThreshold.length; i++) {
            if (aboveThreshold[i] && !isActive) {
                onsets.add(i);
                isActive = true;
            } else if (!aboveThreshold[i] && isActive) {
                offsets.add(i);
                isActive = false;
            }
        }
        if (onsets.size() > offsets.size()) {
            offsets.add(signal.length - 1);
        }

        List<Integer> filteredOnsets = new ArrayList<>();
        List<Integer> filteredOffsets = new ArrayList<>();
        int minDurationSamples = (int)(minDurationSec * fs);

        for (int i = 0; i < onsets.size(); i++) {
            if ((offsets.get(i) - onsets.get(i)) >= minDurationSamples) {
                filteredOnsets.add(onsets.get(i));
                filteredOffsets.add(offsets.get(i));
            }
        }

        // Devolvemos envelopeSmooth para visualizar la envolvente procesada, o 'envelope' para la cruda
        return new ContractionResult(filteredOnsets, filteredOffsets, envelopeSmooth);
    }

    public static RmsResult computeRms(double[] signal, List<Integer> onsets, List<Integer> offsets) {
        List<Double> rmsValues = new ArrayList<>();
        for (int i = 0; i < onsets.size(); i++) {
            int start = onsets.get(i);
            int end = offsets.get(i);
            if (end > start) {
                double[] segment = Arrays.copyOfRange(signal, start, end);
                double meanSquare = Arrays.stream(segment).map(x -> x * x).average().orElse(0);
                rmsValues.add(Math.sqrt(meanSquare));
            }
        }
        double medianRms = 0;
        if (!rmsValues.isEmpty()) {
            org.apache.commons.math3.stat.descriptive.rank.Median median = new org.apache.commons.math3.stat.descriptive.rank.Median();
            medianRms = median.evaluate(rmsValues.stream().mapToDouble(d -> d).toArray());
        }
        return new RmsResult(rmsValues, medianRms);
    }

    public static void plotEMGResults(double fs, double[] filteredSignal, double[] envelope, List<Integer> onsets, List<Integer> offsets, String subjectId) {
        double[] time = IntStream.range(0, filteredSignal.length).mapToDouble(i -> i / fs).toArray();

        XYChart chart = new XYChart(1200, 600);
        chart.setTitle("sEMG with Detected Contractions - Subject " + subjectId);
        chart.setXAxisTitle("Time (s)");
        chart.setYAxisTitle("Amplitude (mV)");
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setPlotGridLinesVisible(true);

        chart.addSeries("Filtered sEMG", time, filteredSignal).setMarker(SeriesMarkers.NONE);

        XYSeries envelopeSeries = chart.addSeries("Envelope", time, envelope);
        envelopeSeries.setLineStyle(new java.awt.BasicStroke(1, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f}, 0.0f));
        envelopeSeries.setMarker(SeriesMarkers.NONE);

        double minY = Arrays.stream(filteredSignal).min().orElse(-1);
        double maxY = Arrays.stream(filteredSignal).max().orElse(1);

        // Dibujar líneas de eventos
        for (int i = 0; i < onsets.size(); i++) {
            double t = onsets.get(i) / fs;
            XYSeries s = chart.addSeries("Onset" + i, new double[]{t, t}, new double[]{minY, maxY});
            s.setLineColor(java.awt.Color.GREEN); s.setMarker(SeriesMarkers.NONE); s.setShowInLegend(false);
        }
        for (int i = 0; i < offsets.size(); i++) {
            double t = offsets.get(i) / fs;
            XYSeries s = chart.addSeries("Offset" + i, new double[]{t, t}, new double[]{minY, maxY});
            s.setLineColor(java.awt.Color.RED); s.setMarker(SeriesMarkers.NONE); s.setShowInLegend(false);
        }

        new SwingWrapper<>(chart).displayChart();
    }

    // --- MAIN ---
    public static void main(String[] args) {
        try {
            String filePath = "C:/Users/phora/OneDrive/Documentos/Uni_Ceu/Tercer año/Segundo Cuatri/Señales Biomédicas/Prácticas/Pablo_Morales/ecg_SERVER_READY.txt";

            String subjectId = "1";

            System.out.println("1. Leyendo archivo EMG...");
            SignalUtils.PatientSignals info = SignalUtils.readAndParseFile(filePath);
            double[] rawData = SignalUtils.stringToDoubleArray(info.dataString);

            System.out.println("   -> Frecuencia: " + info.fs + " Hz, Muestras: " + rawData.length);

            // Conversión
            double[] signalMV = convertToMV(rawData, 3.0, 10, 1000);

            System.out.println("2. Filtrando...");
            double[] passFilteredSignal = SignalUtils.bandpassFilter(signalMV, (double)info.fs, 50, 300, 4);
            double[] filteredSignal = SignalUtils.notchFilter(passFilteredSignal, (double)info.fs, 60.0, 30);

            System.out.println("3. Detectando contracciones...");
            ContractionResult contractions = detectContractions(filteredSignal, (double)info.fs, 0.165, 0.10);

            System.out.println("Resultados:");
            System.out.println("Contracciones detectadas: " + contractions.onsets.size());

            plotEMGResults((double)info.fs, filteredSignal, contractions.envelope, contractions.onsets, contractions.offsets, subjectId);

        } catch (IOException e) {
            System.err.println("Error IO: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
