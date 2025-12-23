package com.example.distance.service;

import com.example.distance.model.RouteRequest;
import com.example.distance.model.RouteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistanceServiceTest {

    @Mock
    OsrmClient osrmClient;

    @InjectMocks
    DistanceService service;

    @Test
    void getRoute_delegatesToClient() {
        RouteRequest.Point from = new RouteRequest.Point(48.85, 2.35);
        RouteRequest.Point to = new RouteRequest.Point(48.87, 2.37);
        RouteRequest req = new RouteRequest();
        req.setFrom(from);
        req.setTo(to);
        RouteResponse expected = new RouteResponse(2.0, "0h 5m 0s");
        when(osrmClient.route(req)).thenReturn(Mono.just(expected));

        RouteResponse res = service.getRoute(req).block();
        assertThat(res).isNotNull();
        assertThat(res.getDistanceKm()).isEqualTo(2.0);
        assertThat(res.getDuration()).isEqualTo("0h 5m 0s");
    }
}