package com.restroute.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("타입 불일치 예외는 INVALID_PARAMETER 응답으로 변환한다")
    void handleTypeMismatch_returnsInvalidParameter() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "page", null, new IllegalArgumentException("bad request"));

        ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatch(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ResponseCode.INVALID_PARAMETER.name());
        assertThat(response.getBody().getMessage()).isEqualTo(ResponseCode.INVALID_PARAMETER.getDefaultMessage());
    }

    @Test
    @DisplayName("일반 예외는 INTERNAL_ERROR 응답으로 변환한다")
    void handleException_returnsInternalError() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ResponseCode.INTERNAL_ERROR.name());
        assertThat(response.getBody().getMessage()).isEqualTo(ResponseCode.INTERNAL_ERROR.getDefaultMessage());
    }
}
