package com.restroute.service.evcharger;

import com.restroute.domain.RestStopEntity;

public record EvChargerDistanceCandidate(RestStopEntity restStop, double distanceMeters) {

    public static EvChargerDistanceCandidate of(RestStopEntity restStop, double distanceMeters) {
        return new EvChargerDistanceCandidate(restStop, distanceMeters);
    }
}
