package com.example.hospital.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateHospitalDTO {

    @NotBlank
    private String name;

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;

    @Valid
    private List<CreateSpecialtyDTO> specialties;

    public CreateHospitalDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public List<CreateSpecialtyDTO> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<CreateSpecialtyDTO> specialties) {
        this.specialties = specialties;
    }
}