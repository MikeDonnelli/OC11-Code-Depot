package com.example.distance.service;

import com.example.distance.model.RouteRequest;
import com.example.distance.model.RouteResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DistanceService {

    private final OsrmClient client;

    public DistanceService(OsrmClient client) {
        this.client = client;
    }

    public Mono<RouteResponse> getRoute(RouteRequest request) {
        return client.route(request);
    }
}
