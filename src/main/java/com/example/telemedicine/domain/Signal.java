package com.example.telemedicine.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@Data
public class Signal {
    private Long id;
    private Long measurementSessionId; //** FK measurementSession
    private LocalDateTime timestamp;
    private SignalType signalType;
    private String patientSignalData;
    private int fs; //sampling frequency

    public Signal(Long id, Long measurementSessionId, LocalDateTime timestamp, SignalType signalType, String patientSignalData, int fs) {
        this.id = id;
        this.measurementSessionId = measurementSessionId;
        this.timestamp = timestamp;
        this.signalType = signalType;
        this.patientSignalData = patientSignalData;
        this.fs = fs;
    }

    /**
     * This method obtains the signal data as a double array
     * @return the data of the signal as an array
     */
    public double[] getSignalDataAsDoubleArray() {
        if (patientSignalData == null || patientSignalData.isBlank()) return new double[0];
        return Arrays.stream(patientSignalData.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    /**
     * This method updates patientSignalData from double[]
     * @param data the data as an Array
     */
    public void setSignalDataFromDoubleArray(double[] data) {
        if (data == null || data.length == 0) {
            this.patientSignalData = "";
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]);
            if (i < data.length - 1) sb.append(",");
        }
        this.patientSignalData = sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signal signal = (Signal) o;
        return fs == signal.fs && Objects.equals(id, signal.id) && Objects.equals(measurementSessionId, signal.measurementSessionId) && Objects.equals(timestamp, signal.timestamp) && signalType == signal.signalType && Objects.equals(patientSignalData, signal.patientSignalData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, measurementSessionId, timestamp, signalType, patientSignalData, fs);
    }

    @Override
    public String toString() {
        return "Signal{" +
                "id=" + id +
                ", measurementSessionId=" + measurementSessionId +
                ", timestamp=" + timestamp +
                ", signalType=" + signalType +
                ", patientSignalData='" + patientSignalData + '\'' +
                ", fs=" + fs +
                '}';
    }
}
