package com.restroute.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.restroute.client.response.OpinetAverageOilPriceResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpinetApiClientTest {

    @Mock
    private OpinetFeignClient opinetFeignClient;

    private OpinetApiClient opinetApiClient;

    @BeforeEach
    void setUp() {
        opinetApiClient = new OpinetApiClient(opinetFeignClient, "https://www.opinet.co.kr", "test-opinet-key");
    }

    @Test
    @DisplayName("전국 평균가 API 호출 시 json 형식과 인증키를 적용한다")
    void getAverageOilPrices_appliesDefaultParameters() {
        OpinetAverageOilPriceResponse response =
                new OpinetAverageOilPriceResponse(new OpinetAverageOilPriceResponse.Result(List.of()));
        when(opinetFeignClient.getAverageOilPrices("json", "test-opinet-key")).thenReturn(response);

        OpinetAverageOilPriceResponse result = opinetApiClient.getAverageOilPrices();

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("전국 평균가 API 실패에 실제 요청 URL과 오류 메시지를 포함한다")
    void getAverageOilPrices_includesRequestUrlWhenApiFails() {
        OpinetAverageOilPriceResponse response = new OpinetAverageOilPriceResponse(null);
        when(opinetFeignClient.getAverageOilPrices("json", "test-opinet-key")).thenReturn(response);

        assertThatThrownBy(opinetApiClient::getAverageOilPrices)
                .isInstanceOf(ExApiException.class)
                .hasMessage(
                        "Failed to fetch API. requestUrl=https://www.opinet.co.kr/api/avgAllPrice.do?out=json&code=test-opinet-key, message=missing RESULT.OIL");
    }

    @Test
    @DisplayName("전국 평균가 API 빈 응답에 실제 요청 URL을 포함한다")
    void getAverageOilPrices_includesRequestUrlWhenResponseIsEmpty() {
        when(opinetFeignClient.getAverageOilPrices("json", "test-opinet-key")).thenReturn(null);

        assertThatThrownBy(opinetApiClient::getAverageOilPrices)
                .isInstanceOf(ExApiException.class)
                .hasMessage(
                        "Failed to fetch API. requestUrl=https://www.opinet.co.kr/api/avgAllPrice.do?out=json&code=test-opinet-key, message=empty response");
    }

    @Test
    @DisplayName("전국 평균가 Feign 예외에 실제 요청 URL과 원인 메시지를 포함한다")
    void getAverageOilPrices_includesRequestUrlWhenFeignCallFails() {
        when(opinetFeignClient.getAverageOilPrices("json", "test-opinet-key"))
                .thenThrow(new IllegalStateException("network down"));

        assertThatThrownBy(opinetApiClient::getAverageOilPrices)
                .isInstanceOf(ExApiException.class)
                .hasMessage(
                        "Failed to fetch API. requestUrl=https://www.opinet.co.kr/api/avgAllPrice.do?out=json&code=test-opinet-key, message=network down")
                .hasCauseInstanceOf(IllegalStateException.class);
    }
}
