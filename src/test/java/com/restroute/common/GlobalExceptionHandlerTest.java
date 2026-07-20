package com.restroute.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.restroute.service.image.InvalidRestStopImageException;
import com.restroute.service.image.RestStopNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    @Test
    @DisplayName("없는 휴게소 예외는 NOT_FOUND 응답으로 변환한다")
    void handleRestStopNotFound_returnsNotFound() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleRestStopNotFound(new RestStopNotFoundException("UNKNOWN"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ResponseCode.NOT_FOUND.name());
    }

    @Test
    @DisplayName("잘못된 이미지는 INVALID_PARAMETER 응답으로 변환한다")
    void handleInvalidRestStopImage_returnsBadRequest() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleInvalidRestStopImage(new InvalidRestStopImageException("invalid"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ResponseCode.INVALID_PARAMETER.name());
    }

    @Test
    @DisplayName("업로드 용량 초과는 INVALID_PARAMETER 응답으로 변환한다")
    void handleMaxUploadSizeExceeded_returnsBadRequest() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleMaxUploadSizeExceeded(new MaxUploadSizeExceededException(20_000_000));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ResponseCode.INVALID_PARAMETER.name());
    }
}
