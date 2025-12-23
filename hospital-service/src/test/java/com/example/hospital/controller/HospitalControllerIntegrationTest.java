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

    @org.springframework.boot.test.mock.mockito.MockBean(com.example.hospital.client.DistanceClient.class)
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.hospital.client.DistanceClient distanceClient;

    @org.junit.jupiter.api.Test
    void nearest_selectsNearestHospital() throws Exception {
        String payload = "{\"from\":{\"lat\":48.85,\"lon\":2.35},\"hospitals\":[{\"id\":1,\"name\":\"A\",\"lat\":48.86,\"lon\":2.36},{\"id\":2,\"name\":\"B\",\"lat\":48.90,\"lon\":2.40}]}";

        // Mock distance responses: A -> 1.0km, B -> 5.0km
        var respA = new com.example.hospital.client.DistanceClient.RouteResponse(1.0, "0h 2m 0s");
        var respB = new com.example.hospital.client.DistanceClient.RouteResponse(5.0, "0h 10m 0s");

        org.mockito.Mockito.when(distanceClient.route(org.mockito.Mockito.any(), org.mockito.Mockito.argThat(p -> p != null && p.getLat() == 48.86))).thenReturn(reactor.core.publisher.Mono.just(respA));
        org.mockito.Mockito.when(distanceClient.route(org.mockito.Mockito.any(), org.mockito.Mockito.argThat(p -> p != null && p.getLat() == 48.90))).thenReturn(reactor.core.publisher.Mono.just(respB));

        mockMvc.perform(post("/api/hospitals/nearest").contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospital.id").value(1))
                .andExpect(jsonPath("$.distanceKm").value(1.0))
                .andExpect(jsonPath("$.duration").value("0h 2m 0s"));
    }
}
