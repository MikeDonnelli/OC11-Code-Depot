package com.example.distance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OsrmClientTest {

    ObjectMapper mapper = new ObjectMapper();
    OsrmClient client = new OsrmClient(org.springframework.web.reactive.function.client.WebClient.builder(), "https://router.project-osrm.org");

    @Test
    void parsesStandardResponse() throws Exception {
        String json = "{ \"routes\": [ { \"distance\": 1234.0, \"duration\": 200.0 } ] }";
        JsonNode n = mapper.readTree(json);
        var res = client.toRouteResponse(n);
        assertThat(res).isNotNull();
        assertThat(res.getDistanceKm()).isEqualTo(1.234);
        assertThat(res.getDuration()).isEqualTo("0h 3m 20s");
    }

    @Test
    void handlesMissingRoutes() throws Exception {
        String json = "{ }";
        JsonNode n = mapper.readTree(json);
        var res = client.toRouteResponse(n);
        assertThat(res.getDistanceKm()).isEqualTo(0.0);
        assertThat(res.getDuration()).isEqualTo("0h 0m 0s");
    }

    @Test
    void formatsLongDurationAndRounding() throws Exception {
        String json = "{ \"routes\": [ { \"distance\": 12345.678, \"duration\": 3661 } ] }";
        JsonNode n = mapper.readTree(json);
        var res = client.toRouteResponse(n);
        assertThat(res.getDistanceKm()).isEqualTo(12.346); // 12345.678 m -> 12.345678 km -> rounded to 12.346
        assertThat(res.getDuration()).isEqualTo("1h 1m 1s");
    }
}
