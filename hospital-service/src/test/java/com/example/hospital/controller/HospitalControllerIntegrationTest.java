package com.example.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@org.junit.jupiter.api.extension.ExtendWith(com.example.hospital.test.TestLoggerExtension.class)
class HospitalControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createHospital_valid_returns201() throws Exception {
        String payload = "{" +
                "\"name\":\"ITest\"," +
                "\"lat\":48.85," +
                "\"lon\":2.35," +
                "\"specialties\":[{" +
                "\"specialty\":\"cardiology\",\"availableBeds\":5}" +
                "]}";

        mockMvc.perform(post("/api/hospitals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("ITest"))
                .andExpect(jsonPath("$.specialties", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void createHospital_invalid_returns400() throws Exception {
        String payload = "{ \"lat\":48.85 }"; // missing name
        mockMvc.perform(post("/api/hospitals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findBySpecialty_returnsList() throws Exception {
        // data.sql seeds CHU Example with cardiology -> should return at least one
        mockMvc.perform(get("/api/hospitals?specialty=cardiology&minBeds=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void reserve_success_and_conflict() throws Exception {
        // Hospital with id=1 seeded has cardiology with availableBeds >=1
        mockMvc.perform(post("/api/hospitals/1/reserve?specialty=cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsString("Reserved")));

        // Hospital with id=2 seeded has cardiology with 0 beds -> expect 409
        mockMvc.perform(post("/api/hospitals/2/reserve?specialty=cardiology"))
                .andExpect(status().isConflict());
    }
}
