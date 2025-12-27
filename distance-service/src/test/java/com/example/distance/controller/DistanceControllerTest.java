package com.example.distance.controller;

import com.example.distance.model.RouteRequest;
import com.example.distance.model.RouteResponse;
import com.example.distance.service.DistanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DistanceController.class)
class DistanceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    DistanceService service;

    @Test
    void route_returnsData() throws Exception {
        RouteResponse resp = new RouteResponse(1.234, "0h 3m 20s");
        when(service.getRoute(any(RouteRequest.class))).thenReturn(Mono.just(resp));

        String payload = "{\"from\":{\"lat\":48.85,\"lon\":2.35},\"to\":{\"lat\":48.87,\"lon\":2.37}}";

        mockMvc.perform(post("/api/distance/route").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(1.234))
                .andExpect(jsonPath("$.duration").value("0h 3m 20s"));
    }

    @Test
    void route_returns502OnError() throws Exception {
        when(service.getRoute(any(RouteRequest.class))).thenReturn(Mono.error(new RuntimeException("osrm down")));

        String payload = "{\"from\":{\"lat\":48.85,\"lon\":2.35},\"to\":{\"lat\":48.87,\"lon\":2.37}}";

        mockMvc.perform(post("/api/distance/route").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.distanceKm").value(0.0))
                .andExpect(jsonPath("$.duration").value("0h 0m 0s"));
    }
}