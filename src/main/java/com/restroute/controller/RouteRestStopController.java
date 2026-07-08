package com.restroute.controller;

import com.restroute.common.ApiResponse;
import com.restroute.controller.response.RouteRestStopResponse;
import com.restroute.service.route.RouteRestStopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/route-rest-stops")
public class RouteRestStopController {

    private static final int DEFAULT_RADIUS_METERS = 1000;

    private final RouteRestStopService routeRestStopService;

    @GetMapping
    public ResponseEntity<ApiResponse<RouteRestStopResponse>> getRouteRestStops(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam(required = false) String destinationQuery,
            @RequestParam(required = false) Double destinationLat,
            @RequestParam(required = false) Double destinationLng,
            @RequestParam(required = false) String destinationName,
            @RequestParam(required = false, defaultValue = "" + DEFAULT_RADIUS_METERS) int radiusMeters) {
        RouteRestStopResponse response = routeRestStopService.findRouteRestStops(
                originLat, originLng, destinationQuery, destinationLat, destinationLng, destinationName, radiusMeters);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
