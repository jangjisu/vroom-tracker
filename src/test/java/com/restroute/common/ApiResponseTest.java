package com.restroute.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    @DisplayName("success는 성공 코드와 데이터를 담는다")
    void success_containsCodeMessageAndData() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS.name());
        assertThat(response.getMessage()).isEqualTo(ResponseCode.SUCCESS.getDefaultMessage());
        assertThat(response.getData()).isEqualTo("ok");
    }

    @Test
    @DisplayName("error는 응답 코드 기본 메시지를 담는다")
    void error_containsDefaultMessage() {
        ApiResponse<Void> response = ApiResponse.error(ResponseCode.NOT_FOUND);

        assertThat(response.getCode()).isEqualTo(ResponseCode.NOT_FOUND.name());
        assertThat(response.getMessage()).isEqualTo(ResponseCode.NOT_FOUND.getDefaultMessage());
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("error는 사용자 지정 메시지를 담을 수 있다")
    void error_containsCustomMessage() {
        ApiResponse<Void> response = ApiResponse.error(ResponseCode.INVALID_PARAMETER, "page must be number");

        assertThat(response.getCode()).isEqualTo(ResponseCode.INVALID_PARAMETER.name());
        assertThat(response.getMessage()).isEqualTo("page must be number");
        assertThat(response.getData()).isNull();
    }
}
