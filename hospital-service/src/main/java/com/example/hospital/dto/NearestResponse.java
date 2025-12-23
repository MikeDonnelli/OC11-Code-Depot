package com.example.hospital.dto;

public class NearestResponse {
    private HospitalDTO hospital;
    private double distanceKm;
    private String duration;

    public NearestResponse() {}

    public NearestResponse(HospitalDTO hospital, double distanceKm, String duration) {
        this.hospital = hospital;
        this.distanceKm = distanceKm;
        this.duration = duration;
    }

    public HospitalDTO getHospital() { return hospital; }
    public void setHospital(HospitalDTO hospital) { this.hospital = hospital; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}