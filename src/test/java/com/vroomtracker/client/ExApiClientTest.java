package com.vroomtracker.client;

import static com.vroomtracker.support.RestStopTestFixtures.highwayServiceAreaInfoResponse;
import static com.vroomtracker.support.RestStopTestFixtures.restStopDetailResponse;
import static com.vroomtracker.support.RestStopTestFixtures.restStopResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vroomtracker.client.response.HighwayServiceAreaInfoResponse;
import com.vroomtracker.client.response.RestStopDetailResponse;
import com.vroomtracker.client.response.RestStopResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExApiClientTest {

    private static final String API_URL = "https://data.ex.co.kr";

    @Mock
    private ExApiFeignClient exApiFeignClient;

    private ExApiClient exApiClient;

    @BeforeEach
    void setUp() {
        exApiClient = new ExApiClient(exApiFeignClient, API_URL, "test-key");
    }

    @Test
    @DisplayName("휴게소 위치 API 호출 시 공통 인증키와 JSON 포맷을 적용한다")
    void getLocationInfoRest_appliesDefaultParameters() {
        RestStopResponse response = restStopResponse("SUCCESS", "1", List.of());
        when(exApiFeignClient.getLocationInfoRest("test-key", "json", "99", "2"))
                .thenReturn(response);

        RestStopResponse result = exApiClient.getLocationInfoRest(2);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("휴게소 편의시설 API 호출 시 공통 인증키와 JSON 포맷을 적용한다")
    void getConvenienceServiceArea_appliesDefaultParameters() {
        RestStopDetailResponse response = restStopDetailResponse("SUCCESS", "1", List.of());
        when(exApiFeignClient.getConvenienceServiceArea("test-key", "json", "99", "2"))
                .thenReturn(response);

        RestStopDetailResponse result = exApiClient.getConvenienceServiceArea(2);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("고속도로 휴게소 정보 API 호출 시 공통 인증키와 JSON 포맷을 적용한다")
    void getHighwayServiceAreaInfoList_appliesDefaultParameters() {
        HighwayServiceAreaInfoResponse response = highwayServiceAreaInfoResponse("SUCCESS", List.of());
        when(exApiFeignClient.getHighwayServiceAreaInfoList("test-key", "json")).thenReturn(response);

        HighwayServiceAreaInfoResponse result = exApiClient.getHighwayServiceAreaInfoList();

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("휴게소 위치 API 실패에 실제 요청 URL과 일반 오류 메시지를 포함한다")
    void getLocationInfoRest_includesRequestUrlAndResponseMessageWhenApiFails() {
        RestStopResponse response = restStopResponse("ERROR", "1", List.of());
        ReflectionTestUtils.setField(response, "message", "인증키가 유효하지 않습니다.");
        when(exApiFeignClient.getLocationInfoRest("test-key", "json", "99", "2"))
                .thenReturn(response);

        assertThatThrownBy(() -> exApiClient.getLocationInfoRest(2))
                .isInstanceOf(ExApiException.class)
                .hasMessage(
                        "Failed to fetch API. requestUrl=https://data.ex.co.kr/openapi/locationinfo/locationinfoRest?key=test-key&type=json&numOfRows=99&pageNo=2, message=인증키가 유효하지 않습니다.");
    }

    @Test
    @DisplayName("휴게소 편의시설 API 실패에 실제 요청 URL과 upstream 오류 메시지를 포함한다")
    void getConvenienceServiceArea_includesRequestUrlAndUpstreamMessageWhenApiFails() throws Exception {
        String json = """
                {
                  "exception": {
                    "message": "For input string: \\"\\""
                  }
                }
                """;
        RestStopDetailResponse response = new ObjectMapper().readValue(json, RestStopDetailResponse.class);
        when(exApiFeignClient.getConvenienceServiceArea("test-key", "json", "99", "2"))
                .thenReturn(response);

        assertThatThrownBy(() -> exApiClient.getConvenienceServiceArea(2))
                .isInstanceOf(ExApiException.class)
                .hasMessage(
                        "Failed to fetch API. requestUrl=https://data.ex.co.kr/openapi/business/conveniServiceArea?key=test-key&type=json&numOfRows=99&pageNo=2, message=For input string: \"\"");
    }

    @Test
    @DisplayName("고속도로 휴게소 정보 API 빈 응답에 실제 요청 URL을 포함한다")
    void getHighwayServiceAreaInfoList_includesRequestUrlWhenResponseIsEmpty() {
        when(exApiFeignClient.getHighwayServiceAreaInfoList("test-key", "json")).thenReturn(null);

        assertThatThrownBy(exApiClient::getHighwayServiceAreaInfoList)
                .isInstanceOf(ExApiException.class)
                .hasMessage(
                        "Failed to fetch API. requestUrl=https://data.ex.co.kr/openapi/restinfo/hiwaySvarInfoList?key=test-key&type=json, message=empty response");
    }

    @Test
    @DisplayName("Feign 호출 예외에 실제 요청 URL과 원인 메시지를 포함한다")
    void getLocationInfoRest_includesRequestUrlAndCauseWhenFeignCallFails() {
        when(exApiFeignClient.getLocationInfoRest("test-key", "json", "99", "2"))
                .thenThrow(new IllegalStateException("Request Blocked"));

        assertThatThrownBy(() -> exApiClient.getLocationInfoRest(2))
                .isInstanceOf(ExApiException.class)
                .hasMessage(
                        "Failed to fetch API. requestUrl=https://data.ex.co.kr/openapi/locationinfo/locationinfoRest?key=test-key&type=json&numOfRows=99&pageNo=2, message=Request Blocked")
                .hasCauseInstanceOf(IllegalStateException.class);
    }
}
