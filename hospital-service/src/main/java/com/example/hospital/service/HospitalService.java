package com.example.hospital.service;

import com.example.hospital.model.Hospital;
import com.example.hospital.model.SpecialtyAvailability;
import com.example.hospital.repository.HospitalRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HospitalService {

    private final HospitalRepository repository;

    public HospitalService(HospitalRepository repository) {
        this.repository = repository;
    }

    public Hospital create(Hospital h) {
        // Ensure specialties are linked
        if (h.getSpecialties() != null) {
            for (SpecialtyAvailability s : h.getSpecialties()) {
                s.setHospital(h);
            }
        }
        return repository.save(h);
    }

    public Optional<Hospital> findById(Long id) {
        return repository.findById(id);
    }

    public List<Hospital> findBySpecialty(String specialty, int minBeds) {
        return repository.findBySpecialtyWithMinBeds(specialty, minBeds);
    }

    public List<String> getAllSpecialties() {
        return repository.findAllDistinctSpecialties();
    }

    @Transactional
    public boolean reserveBed(Long hospitalId, String specialty) {
        Optional<Hospital> oh = repository.findById(hospitalId);
        if (oh.isEmpty()) return false;
        Hospital h = oh.get();
        for (SpecialtyAvailability s : h.getSpecialties()) {
            if (s.getSpecialty().equalsIgnoreCase(specialty)) {
                if (s.getAvailableBeds() > 0) {
                    s.setAvailableBeds(s.getAvailableBeds() - 1);
                    repository.save(h);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
