package com.example.hospital.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class DistanceClient {
    private static final Logger log = LoggerFactory.getLogger(DistanceClient.class);

    private final WebClient webClient;

    public DistanceClient(WebClient.Builder builder, @Value("${distance.service.base-url:http://localhost:8082}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<RouteResponse> route(Point from, Point to) {
        RouteRequest req = new RouteRequest();
        req.setFrom(from);
        req.setTo(to);
        return webClient.post()
                .uri("/api/distance/route")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(RouteResponse.class)
                .doOnError(e -> log.warn("Distance service call failed", e));
    }

    public static class Point {
        private double lat;
        private double lon;

        public Point() {}

        public Point(double lat, double lon) { this.lat = lat; this.lon = lon; }

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLon() { return lon; }
        public void setLon(double lon) { this.lon = lon; }
    }

    public static class RouteRequest {
        private Point from;
        private Point to;

        public Point getFrom() { return from; }
        public void setFrom(Point from) { this.from = from; }
        public Point getTo() { return to; }
        public void setTo(Point to) { this.to = to; }
    }

    public static class RouteResponse {
        private double distanceKm;
        private String duration;

        public RouteResponse() {}

        public RouteResponse(double distanceKm, String duration) {
            this.distanceKm = distanceKm;
            this.duration = duration;
        }

        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }
}