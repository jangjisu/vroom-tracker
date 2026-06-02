package com.vroomtracker.controller.response;

import com.vroomtracker.domain.HighwayServiceAreaInfoEntity;
import com.vroomtracker.domain.RestStopDetailEntity;
import com.vroomtracker.domain.RestStopEntity;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public record RestStopDetailViewResponse(
        String serviceAreaCode,
        String restStopName,
        String routeName,
        String xValue,
        String yValue,
        String address,
        String convenience,
        String maintenanceYn,
        String truckSaYn,
        String direction,
        Integer compactCarParkingCount,
        Integer fullSizeCarParkingCount,
        Integer disabledParkingCount) {

    public static RestStopDetailViewResponse of(
            RestStopEntity restStop, Optional<RestStopDetailEntity> detail, List<HighwayServiceAreaInfoEntity> infos) {
        String detailAddress = textOf(detail, RestStopDetailEntity::getSvarAddr);
        String fallbackAddress = minText(infos, HighwayServiceAreaInfoEntity::getServiceAreaAddress);

        return new RestStopDetailViewResponse(
                restStop.getServiceAreaCode(),
                restStop.getUnitName(),
                restStop.getRouteName(),
                restStop.getXValue(),
                restStop.getYValue(),
                firstNonNull(detailAddress, fallbackAddress),
                textOf(detail, RestStopDetailEntity::getConvenience),
                textOf(detail, RestStopDetailEntity::getMaintenanceYn),
                textOf(detail, RestStopDetailEntity::getTruckSaYn),
                minText(infos, HighwayServiceAreaInfoEntity::getDirectionTypeName),
                sumIntegerValues(infos, HighwayServiceAreaInfoEntity::getCompactCarParkingCount),
                sumIntegerValues(infos, HighwayServiceAreaInfoEntity::getFullSizeCarParkingCount),
                sumIntegerValues(infos, HighwayServiceAreaInfoEntity::getDisabledParkingCount));
    }

    private static <T> String minText(List<T> items, Function<T, String> getter) {
        return items.stream()
                .map(getter)
                .filter(RestStopDetailViewResponse::hasText)
                .map(String::trim)
                .min(String::compareTo)
                .orElse(null);
    }

    private static String textOf(Optional<RestStopDetailEntity> detail, Function<RestStopDetailEntity, String> getter) {
        return detail.map(getter)
                .filter(RestStopDetailViewResponse::hasText)
                .map(String::trim)
                .orElse(null);
    }

    private static Integer sumIntegerValues(
            List<HighwayServiceAreaInfoEntity> infos, Function<HighwayServiceAreaInfoEntity, String> getter) {
        List<Integer> values = infos.stream()
                .map(getter)
                .map(RestStopDetailViewResponse::parseInteger)
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

    private static String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
