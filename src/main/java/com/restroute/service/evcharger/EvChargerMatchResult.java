package com.restroute.service.evcharger;

record EvChargerMatchResult(String serviceAreaCode, Double distanceMeters, String matchType) {

    static EvChargerMatchResult matched(String serviceAreaCode, double distanceMeters, String matchType) {
        return new EvChargerMatchResult(serviceAreaCode, distanceMeters, matchType);
    }

    static EvChargerMatchResult unmatched(String matchType) {
        return new EvChargerMatchResult(null, null, matchType);
    }
}
