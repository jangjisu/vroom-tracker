package com.vroomtracker.service;

public class RouteRestStopNotFoundException extends RuntimeException {

    public RouteRestStopNotFoundException(String message) {
        super(message);
    }
}
