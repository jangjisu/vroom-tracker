package com.restroute.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RouteRestStopResponseTest {

    @Test
    @DisplayName("경로 결과 응답은 전국 평균가 요약 필드를 노출하지 않는다")
    void routeResponse_doesNotExposeNationalOilPriceSummary() {
        RouteRestStopResponse response = RouteRestStopResponse.of(
                RouteRestStopResponse.Destination.of("부산역", 35.0, 129.0),
                RouteRestStopResponse.RouteSummary.of(100L, 200L, List.of()),
                List.of());

        assertThat(Arrays.stream(RouteRestStopResponse.class.getRecordComponents())
                        .map(component -> component.getName()))
                .doesNotContain("nationalOilPriceSummary");
        assertThat(response.restStops()).isEmpty();
    }
}
