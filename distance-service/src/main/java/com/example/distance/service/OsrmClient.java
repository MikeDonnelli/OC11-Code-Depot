package com.example.distance.service;

import com.example.distance.model.RouteRequest;
import com.example.distance.model.RouteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Component
public class OsrmClient {

    private static final Logger log = LoggerFactory.getLogger(OsrmClient.class);

    private final WebClient webClient;
    private final String baseUrl;

    public OsrmClient(WebClient.Builder builder, @Value("${osrm.baseUrl:https://router.project-osrm.org}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
    }

    public Mono<RouteResponse> route(RouteRequest request) {
        String coords = String.format(Locale.US, "%f,%f;%f,%f",
                request.getFrom().getLon(), request.getFrom().getLat(),
                request.getTo().getLon(), request.getTo().getLat());
        String uri = "/route/v1/driving/" + coords + "?overview=false&alternatives=false&steps=false";
        log.debug("Calling OSRM: {}{}", baseUrl, uri);
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::toRouteResponse);
    }

    RouteResponse toRouteResponse(JsonNode n) {
        if (n.has("routes") && n.get("routes").isArray() && n.get("routes").size() > 0) {
            JsonNode r = n.get("routes").get(0);
            double distanceMeters = r.has("distance") ? r.get("distance").asDouble(0.0) : 0.0;
            double durationSeconds = r.has("duration") ? r.get("duration").asDouble(0.0) : 0.0;

            // convert meters -> kilometers with 1 decimal precision
            double distanceKm = Math.round((distanceMeters / 1000.0) * 10.0) / 10.0;

            long seconds = Math.round(durationSeconds);
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            
            String duration;
            if (hours > 0) {
                duration = String.format("%dh %dm %ds", hours, minutes, secs);
            } else if (minutes > 0) {
                duration = String.format("%dm %ds", minutes, secs);
            } else {
                duration = String.format("%ds", secs);
            }

            return new RouteResponse(distanceKm, duration);
        }
        return new RouteResponse(0.0, "0h 0m 0s");
    }
}