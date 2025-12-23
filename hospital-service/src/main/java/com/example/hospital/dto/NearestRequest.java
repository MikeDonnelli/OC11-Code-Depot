package com.example.hospital.dto;

import com.example.hospital.client.DistanceClient.Point;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class NearestRequest {
    @NotNull
    private Point from;

    @NotNull
    private List<HospitalDTO> hospitals;

    public Point getFrom() { return from; }
    public void setFrom(Point from) { this.from = from; }
    public List<HospitalDTO> getHospitals() { return hospitals; }
    public void setHospitals(List<HospitalDTO> hospitals) { this.hospitals = hospitals; }
}