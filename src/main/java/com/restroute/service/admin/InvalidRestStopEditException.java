package com.restroute.service.admin;

public class InvalidRestStopEditException extends RuntimeException {

    public InvalidRestStopEditException(String message) {
        super(message);
    }

    public static InvalidRestStopEditException forInvalidCoordinate(String value) {
        return new InvalidRestStopEditException("Invalid coordinate value: " + value);
    }
}
