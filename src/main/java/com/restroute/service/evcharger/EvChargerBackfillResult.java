package com.restroute.service.evcharger;

public record EvChargerBackfillResult(int stationCount, int matchedCount, int unmatchedCount) {

    public static EvChargerBackfillResult of(int stationCount, int matchedCount, int unmatchedCount) {
        return new EvChargerBackfillResult(stationCount, matchedCount, unmatchedCount);
    }
}
