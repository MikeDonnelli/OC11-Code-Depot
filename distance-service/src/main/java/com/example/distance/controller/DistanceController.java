package com.example.distance.controller;

import com.example.distance.model.RouteRequest;
import com.example.distance.model.RouteResponse;
import com.example.distance.service.DistanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/distance")
public class DistanceController {

    private final DistanceService service;

    public DistanceController(DistanceService service) {
        this.service = service;
    }

    @PostMapping("/route")
    public ResponseEntity<RouteResponse> route(@RequestBody RouteRequest request) {
        try {
            RouteResponse res = service.getRoute(request).block();
            return ResponseEntity.ok(res == null ? new RouteResponse(0.0, "0h 0m 0s") : res);
        } catch (Exception e) {
            return ResponseEntity.status(502).body(new RouteResponse(0.0, "0h 0m 0s"));
        }
    }
}