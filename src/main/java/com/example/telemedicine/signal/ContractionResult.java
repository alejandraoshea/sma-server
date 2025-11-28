package com.example.telemedicine.signal;

import lombok.Data;

import java.util.List;

/**
 * Represents the result of detecting muscle contractions from an EMG signal.
 */
@Data
public class ContractionResult {
    public final List<Integer> onsets;
    public final List<Integer> offsets;
    public final double[] envelope;

    /**
     * Contains:
     * @param onsets List of start contractions sample indexes.
     * @param offsets List of end contractions sample indexes.
     * @param envelope Processed signal envelope used for detection.
     */
    public ContractionResult(List<Integer> onsets, List<Integer> offsets, double[] envelope) {
        this.onsets = onsets;
        this.offsets = offsets;
        this.envelope = envelope;
    }
}
