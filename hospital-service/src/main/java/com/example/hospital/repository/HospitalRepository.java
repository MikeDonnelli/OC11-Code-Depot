package com.example.hospital.repository;

import com.example.hospital.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // Simple query to fetch hospitals that have at least one specialty with name = :specialty and availableBeds >= :minBeds
    @Query("select h from Hospital h join h.specialties s where lower(s.specialty) = lower(:specialty) and s.availableBeds >= :minBeds")
    List<Hospital> findBySpecialtyWithMinBeds(@Param("specialty") String specialty, @Param("minBeds") int minBeds);
}
