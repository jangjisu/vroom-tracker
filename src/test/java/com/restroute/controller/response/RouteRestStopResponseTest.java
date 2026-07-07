package com.restroute.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RouteRestStopResponseTest {

    @Test
    @DisplayName("기존 factory는 전국 평균가 요약 없이 경로 결과 응답을 만든다")
    void of_withoutNationalOilPriceSummary_returnsResponse() {
        RouteRestStopResponse response = RouteRestStopResponse.of(
                RouteRestStopResponse.Destination.of("부산역", 35.0, 129.0),
                RouteRestStopResponse.RouteSummary.of(100L, 200L, List.of()),
                List.of());

        assertThat(response.nationalOilPriceSummary()).isNull();
        assertThat(response.restStops()).isEmpty();
    }
}
