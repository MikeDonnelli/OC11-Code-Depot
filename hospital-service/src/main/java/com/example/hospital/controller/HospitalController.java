package com.example.hospital.controller;

import com.example.hospital.model.Hospital;
import com.example.hospital.service.HospitalService;
import com.example.hospital.client.DistanceClient;
import com.example.hospital.dto.NearestRequest;
import com.example.hospital.dto.NearestResponse;
import com.example.hospital.dto.HospitalDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService service;
    private final DistanceClient distanceClient;

    public HospitalController(HospitalService service, DistanceClient distanceClient) {
        this.service = service;
        this.distanceClient = distanceClient;
    }

    @PostMapping
    public ResponseEntity<com.example.hospital.dto.HospitalDTO> create(@jakarta.validation.Valid @RequestBody com.example.hospital.dto.CreateHospitalDTO dto) {
        Hospital h = com.example.hospital.mapper.HospitalMapper.fromCreateDto(dto);
        Hospital created = service.create(h);
        com.example.hospital.dto.HospitalDTO out = com.example.hospital.mapper.HospitalMapper.toDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    @GetMapping
    public List<Hospital> findBySpecialty(@RequestParam(required = false) String specialty, @RequestParam(defaultValue = "1") int minBeds) {
        if (specialty == null || specialty.isEmpty()) {
            // Return all hospitals when specialty is not provided
            return service.findAll();
        }
        return service.findBySpecialty(specialty, minBeds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/specialties")
    public List<String> getAllSpecialties() {
        return service.getAllSpecialties();
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<String> reserve(@PathVariable Long id, @RequestParam String specialty) {
        boolean ok = service.reserveBed(id, specialty);
        if (ok) return ResponseEntity.ok("Reserved");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("No beds available or specialty not found");
    }

    @PostMapping("/nearest")
    public ResponseEntity<NearestResponse> nearest(@jakarta.validation.Valid @RequestBody NearestRequest req) {
        if (req.getHospitals() == null || req.getHospitals().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        NearestResponse best = null;
        for (HospitalDTO h : req.getHospitals()) {
            DistanceClient.Point to = new DistanceClient.Point(h.getLat(), h.getLon());
            DistanceClient.Point from = req.getFrom();
            try {
                var r = distanceClient.route(from, to).block();
                if (r == null) continue;
                if (best == null || r.getDistanceKm() < best.getDistanceKm()) {
                    best = new NearestResponse(h, r.getDistanceKm(), r.getDuration());
                }
            } catch (Exception e) {
                // ignore failing entries
            }
        }

        if (best == null) return ResponseEntity.status(502).build();
        return ResponseEntity.ok(best);
    }
}
