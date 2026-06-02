package com.vroomtracker.controller.response;

import com.vroomtracker.domain.RestStopEntity;

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
            RestStopEntity restStop,
            String address,
            String convenience,
            String maintenanceYn,
            String truckSaYn,
            String direction,
            Integer compactCarParkingCount,
            Integer fullSizeCarParkingCount,
            Integer disabledParkingCount) {
        return new RestStopDetailViewResponse(
                restStop.getServiceAreaCode(),
                restStop.getUnitName(),
                restStop.getRouteName(),
                restStop.getXValue(),
                restStop.getYValue(),
                address,
                convenience,
                maintenanceYn,
                truckSaYn,
                direction,
                compactCarParkingCount,
                fullSizeCarParkingCount,
                disabledParkingCount);
    }
}
