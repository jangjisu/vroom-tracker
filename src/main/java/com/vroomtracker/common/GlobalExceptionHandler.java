package com.vroomtracker.common;

import com.vroomtracker.client.KakaoApiException;
import com.vroomtracker.service.RouteRestStopNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Request parameter type mismatch: {}", e.getMessage());
        return ResponseEntity.status(ResponseCode.INVALID_PARAMETER.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.INVALID_PARAMETER));
    }

    @ExceptionHandler(RouteRestStopNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRouteRestStopNotFound(RouteRestStopNotFoundException e) {
        log.warn("Route rest stops not found: {}", e.getMessage());
        return ResponseEntity.status(ResponseCode.NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(KakaoApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleKakaoApiException(KakaoApiException e) {
        log.error("Kakao API call failed", e);
        return ResponseEntity.status(ResponseCode.EXTERNAL_API_UNAVAILABLE.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.EXTERNAL_API_UNAVAILABLE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(ResponseCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.INTERNAL_ERROR));
    }
}
