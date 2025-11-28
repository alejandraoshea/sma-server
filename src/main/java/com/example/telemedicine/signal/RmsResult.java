package com.example.telemedicine.signal;

import lombok.Data;

import java.util.List;

/**
 * Represents the Root Mean Square values detected for EMG contractions.
 * There are used the median and the RMS list values for each contraction.
 */
@Data
public class RmsResult {
    public final List<Double> rmsValues;
    public final double medianRms;

    public RmsResult(List<Double> rmsValues, double medianRms) {
        this.rmsValues = rmsValues;
        this.medianRms = medianRms;
    }
}
