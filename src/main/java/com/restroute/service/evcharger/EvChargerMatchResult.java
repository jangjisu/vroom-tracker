package com.restroute.service.evcharger;

public record EvChargerMatchResult(String serviceAreaCode, Double distanceMeters, String matchType) {

    public static EvChargerMatchResult matched(String serviceAreaCode, double distanceMeters, String matchType) {
        return new EvChargerMatchResult(serviceAreaCode, distanceMeters, matchType);
    }

    public static EvChargerMatchResult unmatched(String matchType) {
        return new EvChargerMatchResult(null, null, matchType);
    }
}
