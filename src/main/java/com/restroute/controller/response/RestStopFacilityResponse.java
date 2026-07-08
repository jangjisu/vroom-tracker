package com.restroute.controller.response;

import com.restroute.domain.HighwayServiceAreaInfoEntity;
import com.restroute.domain.RestStopDetailEntity;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public record RestStopFacilityResponse(
        String convenience,
        String maintenanceYn,
        String truckSaYn,
        String direction,
        Integer compactCarParkingCount,
        Integer fullSizeCarParkingCount,
        Integer disabledParkingCount) {

    public static RestStopFacilityResponse of(
            Optional<RestStopDetailEntity> detail, List<HighwayServiceAreaInfoEntity> infos) {
        return new RestStopFacilityResponse(
                textOf(detail, RestStopDetailEntity::getConvenience),
                textOf(detail, RestStopDetailEntity::getMaintenanceYn),
                textOf(detail, RestStopDetailEntity::getTruckSaYn),
                minText(infos, HighwayServiceAreaInfoEntity::getDirectionTypeName),
                sumIntegerValues(infos, HighwayServiceAreaInfoEntity::getCompactCarParkingCount),
                sumIntegerValues(infos, HighwayServiceAreaInfoEntity::getFullSizeCarParkingCount),
                sumIntegerValues(infos, HighwayServiceAreaInfoEntity::getDisabledParkingCount));
    }

    private static String textOf(Optional<RestStopDetailEntity> detail, Function<RestStopDetailEntity, String> getter) {
        return detail.map(getter)
                .filter(RestStopFacilityResponse::hasText)
                .map(String::trim)
                .orElse(null);
    }

    private static <T> String minText(List<T> items, Function<T, String> getter) {
        return items.stream()
                .map(getter)
                .filter(RestStopFacilityResponse::hasText)
                .map(String::trim)
                .min(String::compareTo)
                .orElse(null);
    }

    private static Integer sumIntegerValues(
            List<HighwayServiceAreaInfoEntity> infos, Function<HighwayServiceAreaInfoEntity, String> getter) {
        List<Integer> values = infos.stream()
                .map(getter)
                .map(RestStopFacilityResponse::parseInteger)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) {
            return null;
        }

        return values.stream().mapToInt(Integer::intValue).sum();
    }

    private static Integer parseInteger(String value) {
        if (!hasText(value)) {
            return null;
        }

        return Integer.valueOf(value.trim());
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
