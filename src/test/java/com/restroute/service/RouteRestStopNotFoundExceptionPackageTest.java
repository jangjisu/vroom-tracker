package com.restroute.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.restroute.service.route.RouteRestStopNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RouteRestStopNotFoundExceptionPackageTest {

    @Test
    @DisplayName("경로 추천 예외는 route 서비스 패키지에 속한다")
    void routeRestStopNotFoundException_belongsToRoutePackage() {
        assertThat(RouteRestStopNotFoundException.class.getPackageName()).isEqualTo("com.restroute.service.route");
    }
}
