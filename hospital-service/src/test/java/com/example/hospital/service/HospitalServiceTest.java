package com.example.hospital.service;

import com.example.hospital.model.Hospital;
import com.example.hospital.model.SpecialtyAvailability;
import com.example.hospital.repository.HospitalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, com.example.hospital.test.TestLoggerExtension.class})
class HospitalServiceTest {

    @Mock
    HospitalRepository repository;

    @InjectMocks
    HospitalService service;

    @Test
    void reserveBed_success() {
        Hospital h = new Hospital("Test", 1.0, 2.0);
        SpecialtyAvailability s = new SpecialtyAvailability("cardiology", 2);
        h.addSpecialty(s);
        when(repository.findById(1L)).thenReturn(Optional.of(h));
        when(repository.save(any(Hospital.class))).thenReturn(h);

        boolean ok = service.reserveBed(1L, "cardiology");

        assertThat(ok).isTrue();
        assertThat(h.getSpecialties().get(0).getAvailableBeds()).isEqualTo(1);
        verify(repository, times(1)).save(h);
    }

    @Test
    void reserveBed_noBeds() {
        Hospital h = new Hospital("Test", 1.0, 2.0);
        SpecialtyAvailability s = new SpecialtyAvailability("cardiology", 0);
        h.addSpecialty(s);
        when(repository.findById(1L)).thenReturn(Optional.of(h));

        boolean ok = service.reserveBed(1L, "cardiology");

        assertThat(ok).isFalse();
        assertThat(h.getSpecialties().get(0).getAvailableBeds()).isEqualTo(0);
        verify(repository, never()).save(any());
    }

    @Test
    void findBySpecialty_delegatesToRepo() {
        when(repository.findBySpecialtyWithMinBeds("cardiology", 1)).thenReturn(List.of());
        List<Hospital> res = service.findBySpecialty("cardiology", 1);
        assertThat(res).isEmpty();
        verify(repository, times(1)).findBySpecialtyWithMinBeds("cardiology", 1);
    }
}
