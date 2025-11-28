package com.example.telemedicine.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.json.JSONPropertyName;

import java.util.Objects;

@Data
public class Locality {
    private Long localityId;
    private String name;
    private Double latitude;
    private Double longitude;

    public Locality() {
    }

    public Locality(Long localityId, String name, Double latitude, Double longitude) {
        this.localityId = localityId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locality locality = (Locality) o;
        return Objects.equals(localityId, locality.localityId) && Objects.equals(name, locality.name) && Objects.equals(latitude, locality.latitude) && Objects.equals(longitude, locality.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localityId, name, latitude, longitude);
    }

    @Override
    public String toString() {
        return "Locality{" +
                "localityId=" + localityId +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
