package com.restroute.service.image;

public class RestStopNotFoundException extends RuntimeException {

    public RestStopNotFoundException(String serviceAreaCode) {
        super("Rest stop not found: " + serviceAreaCode);
    }

    public static RestStopNotFoundException forServiceAreaCode(String serviceAreaCode) {
        return new RestStopNotFoundException(serviceAreaCode);
    }
}
