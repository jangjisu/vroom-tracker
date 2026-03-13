package com.vroomtracker.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    SUCCESS(HttpStatus.OK, "OK"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid request parameter"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    EXTERNAL_API_UNAVAILABLE(HttpStatus.OK, "External API temporarily unavailable"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
