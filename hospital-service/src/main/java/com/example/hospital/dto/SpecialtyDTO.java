package com.example.hospital.dto;

public class SpecialtyDTO {
    private Long id;
    private String specialty;
    private int availableBeds;

    public SpecialtyDTO() {}

    public SpecialtyDTO(Long id, String specialty, int availableBeds) {
        this.id = id;
        this.specialty = specialty;
        this.availableBeds = availableBeds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public int getAvailableBeds() {
        return availableBeds;
    }

    public void setAvailableBeds(int availableBeds) {
        this.availableBeds = availableBeds;
    }
}