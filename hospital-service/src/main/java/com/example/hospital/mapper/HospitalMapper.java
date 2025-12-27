package com.example.hospital.mapper;

import com.example.hospital.dto.CreateHospitalDTO;
import com.example.hospital.dto.CreateSpecialtyDTO;
import com.example.hospital.dto.HospitalDTO;
import com.example.hospital.dto.SpecialtyDTO;
import com.example.hospital.model.Hospital;
import com.example.hospital.model.SpecialtyAvailability;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HospitalMapper {

    public static HospitalDTO toDto(Hospital h) {
        if (h == null) return null;
        HospitalDTO dto = new HospitalDTO();
        dto.setId(h.getId());
        dto.setName(h.getName());
        dto.setLat(h.getLat());
        dto.setLon(h.getLon());
        List<SpecialtyDTO> s = h.getSpecialties() == null ? Collections.emptyList() : h.getSpecialties().stream().map(sa -> {
            SpecialtyDTO sd = new SpecialtyDTO();
            sd.setId(sa.getId());
            sd.setSpecialty(sa.getSpecialty());
            sd.setAvailableBeds(sa.getAvailableBeds());
            return sd;
        }).collect(Collectors.toList());
        dto.setSpecialties(s);
        return dto;
    }

    public static Hospital fromCreateDto(CreateHospitalDTO dto) {
        if (dto == null) return null;
        Hospital h = new Hospital();
        h.setName(dto.getName());
        h.setLat(dto.getLat());
        h.setLon(dto.getLon());
        if (dto.getSpecialties() != null) {
            for (CreateSpecialtyDTO csd : dto.getSpecialties()) {
                SpecialtyAvailability sa = new SpecialtyAvailability();
                sa.setSpecialty(csd.getSpecialty());
                sa.setAvailableBeds(csd.getAvailableBeds());
                h.addSpecialty(sa);
            }
        }
        return h;
    }
}
