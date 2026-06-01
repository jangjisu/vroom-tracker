package com.vroomtracker.client.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RestStopDetailResponseTest {

    @Test
    @DisplayName("code가 SUCCESS이면 성공으로 판단한다")
    void isSuccess_returnsTrueForSuccessCode() {
        RestStopDetailResponse response = new RestStopDetailResponse();
        ReflectionTestUtils.setField(response, "code", "SUCCESS");

        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("code가 SUCCESS가 아니면 실패로 판단한다")
    void isSuccess_returnsFalseForOtherCode() {
        RestStopDetailResponse response = new RestStopDetailResponse();
        ReflectionTestUtils.setField(response, "code", "ERROR");

        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("pageSize를 총 페이지 수로 변환한다")
    void getTotalPageCount_returnsParsedValue() {
        RestStopDetailResponse response = new RestStopDetailResponse();
        ReflectionTestUtils.setField(response, "pageSize", "3");

        assertThat(response.getTotalPageCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("pageSize가 숫자가 아니면 총 페이지 수 기본값 1을 반환한다")
    void getTotalPageCount_returnsDefaultWhenInvalid() {
        RestStopDetailResponse response = new RestStopDetailResponse();
        ReflectionTestUtils.setField(response, "pageSize", "abc");

        assertThat(response.getTotalPageCount()).isEqualTo(1);
    }
}
