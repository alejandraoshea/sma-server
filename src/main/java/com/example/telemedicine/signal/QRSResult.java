package com.example.telemedicine.signal;

import lombok.Data;

import java.util.List;

@Data
public class QRSResult {
    public final List<Integer> rPeaks;
    public final List<Integer> qPeaks;

    public QRSResult(List<Integer> r, List<Integer> q) {
        this.rPeaks = r;
        this.qPeaks = q;
    }
}
