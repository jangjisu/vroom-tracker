package com.restroute.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ResponseCodeTest {

    @Test
    @DisplayName("응답 코드는 HTTP 상태와 기본 메시지를 가진다")
    void responseCode_containsHttpStatusAndDefaultMessage() {
        assertThat(ResponseCode.SUCCESS.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(ResponseCode.SUCCESS.getDefaultMessage()).isEqualTo("OK");
        assertThat(ResponseCode.INVALID_PARAMETER.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ResponseCode.INTERNAL_ERROR.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
