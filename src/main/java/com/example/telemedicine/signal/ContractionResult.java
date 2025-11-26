package com.example.telemedicine.signal;

import lombok.Data;

import java.util.List;

@Data
public class ContractionResult {
    public final List<Integer> onsets;
    public final List<Integer> offsets;
    public final double[] envelope;

    public ContractionResult(List<Integer> onsets, List<Integer> offsets, double[] envelope) {
        this.onsets = onsets;
        this.offsets = offsets;
        this.envelope = envelope;
    }
}
