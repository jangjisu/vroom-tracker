package com.restroute.controller.response;

import com.restroute.domain.RestStopDetailEntity;
import com.restroute.domain.RestStopEntity;
import java.util.Optional;
import java.util.function.Function;

public record RestStopBasicInfoResponse(
        String serviceAreaCode,
        String unitCode,
        String unitName,
        String routeNo,
        String routeName,
        String xValue,
        String yValue,
        String stdRestCd,
        String address,
        String telNo,
        String brand,
        int evChargerCount,
        String detailImageUrl) {

    public static RestStopBasicInfoResponse of(
            RestStopEntity restStop, Optional<RestStopDetailEntity> detail, int evChargerCount, String detailImageUrl) {
        return new RestStopBasicInfoResponse(
                restStop.getServiceAreaCode(),
                restStop.getUnitCode(),
                restStop.getUnitName(),
                restStop.getRouteNo(),
                restStop.getRouteName(),
                restStop.getXValue(),
                restStop.getYValue(),
                restStop.getStdRestCd(),
                textOf(detail, RestStopDetailEntity::getSvarAddr),
                textOf(detail, RestStopDetailEntity::getTelNo),
                textOf(detail, RestStopDetailEntity::getBrand),
                evChargerCount,
                detailImageUrl);
    }

    private static String textOf(Optional<RestStopDetailEntity> detail, Function<RestStopDetailEntity, String> getter) {
        return detail.map(getter)
                .filter(RestStopBasicInfoResponse::hasText)
                .map(String::trim)
                .orElse(null);
    }

    private static boolean hasText(String value) {
        return !value.trim().isEmpty();
    }
}
