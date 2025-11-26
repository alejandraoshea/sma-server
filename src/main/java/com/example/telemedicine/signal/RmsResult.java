package com.example.telemedicine.signal;

import lombok.Data;

import java.util.List;

@Data
public class RmsResult {
    public final List<Double> rmsValues;
    public final double medianRms;

    public RmsResult(List<Double> rmsValues, double medianRms) {
        this.rmsValues = rmsValues;
        this.medianRms = medianRms;
    }
}
