package com.vroomtracker.client;

import static com.vroomtracker.support.RestStopTestFixtures.restStopResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.vroomtracker.client.response.RestStopResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExApiClientTest {

    @Mock
    private ExApiFeignClient exApiFeignClient;

    private ExApiClient exApiClient;

    @BeforeEach
    void setUp() {
        exApiClient = new ExApiClient(exApiFeignClient, "test-key");
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
}
