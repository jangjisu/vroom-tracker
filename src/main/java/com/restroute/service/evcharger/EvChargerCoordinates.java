package com.restroute.service.evcharger;

record EvChargerCoordinates(double latitude, double longitude) {

    static EvChargerCoordinates of(double latitude, double longitude) {
        return new EvChargerCoordinates(latitude, longitude);
    }
}
