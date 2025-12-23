package com.example.hospital.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double lat;
    private double lon;

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SpecialtyAvailability> specialties = new ArrayList<>();

    public Hospital() {}

    public Hospital(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public List<SpecialtyAvailability> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<SpecialtyAvailability> specialties) {
        this.specialties = specialties;
    }

    public void addSpecialty(SpecialtyAvailability s) {
        s.setHospital(this);
        this.specialties.add(s);
    }
}
