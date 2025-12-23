package com.example.distance.model;

/**
 * Response returned by the distance service.
 * - distanceKm: distance in kilometers (decimal, e.g. 1.234)
 * - duration: human readable format "Hh Mm Ss" (e.g. "0h 3m 20s")
 */
public class RouteResponse {
    private double distanceKm;
    private String duration;

    public RouteResponse() {}

    public RouteResponse(double distanceKm, String duration) {
        this.distanceKm = distanceKm;
        this.duration = duration;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}