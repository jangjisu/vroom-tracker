package com.restroute.service.evcharger;

import com.restroute.domain.RestStopEntity;

record EvChargerDistanceCandidate(RestStopEntity restStop, double distanceMeters) {

    static EvChargerDistanceCandidate of(RestStopEntity restStop, double distanceMeters) {
        return new EvChargerDistanceCandidate(restStop, distanceMeters);
    }
}
