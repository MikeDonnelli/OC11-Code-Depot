package com.example.hospital.controller;

import com.example.hospital.model.Hospital;
import com.example.hospital.service.HospitalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService service;

    public HospitalController(HospitalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<com.example.hospital.dto.HospitalDTO> create(@jakarta.validation.Valid @RequestBody com.example.hospital.dto.CreateHospitalDTO dto) {
        Hospital h = com.example.hospital.mapper.HospitalMapper.fromCreateDto(dto);
        Hospital created = service.create(h);
        com.example.hospital.dto.HospitalDTO out = com.example.hospital.mapper.HospitalMapper.toDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    @GetMapping
    public List<Hospital> findBySpecialty(@RequestParam String specialty, @RequestParam(defaultValue = "1") int minBeds) {
        return service.findBySpecialty(specialty, minBeds);
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<String> reserve(@PathVariable Long id, @RequestParam String specialty) {
        boolean ok = service.reserveBed(id, specialty);
        if (ok) return ResponseEntity.ok("Reserved");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("No beds available or specialty not found");
    }
}
