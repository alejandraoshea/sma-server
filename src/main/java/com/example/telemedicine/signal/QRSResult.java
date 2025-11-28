package com.example.telemedicine.signal;

import lombok.Data;

import java.util.List;

/**
 * Represents the detected QRS complexes in an ECG signal, contains indices for
 * either R-peaks and Q-peaks.
 */
@Data
public class QRSResult {
    public final List<Integer> rPeaks;
    public final List<Integer> qPeaks;

    /**
     * @param r List of R-peaks indices.
     * @param q List of Q-peaks indices corresponding to each R-peak.
     */
    public QRSResult(List<Integer> r, List<Integer> q) {
        this.rPeaks = r;
        this.qPeaks = q;
    }
}
