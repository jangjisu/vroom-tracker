package com.restroute.controller.response;

import com.restroute.domain.RestStopDetailEntity;
import com.restroute.domain.RestStopEntity;

public record AdminRestStopEditableResponse(
        String serviceAreaCode,
        String unitCode,
        String unitName,
        String routeNo,
        String routeName,
        String xValue,
        String yValue,
        String telNo,
        String brand,
        String routeCode,
        String svarAddr,
        String convenience,
        String maintenanceYn,
        String truckSaYn,
        boolean adminOverridden) {

    public static AdminRestStopEditableResponse of(RestStopEntity restStop, RestStopDetailEntity detail) {
        return new AdminRestStopEditableResponse(
                restStop.getServiceAreaCode(),
                restStop.getUnitCode(),
                restStop.getUnitName(),
                restStop.getRouteNo(),
                restStop.getRouteName(),
                restStop.getXValue(),
                restStop.getYValue(),
                detail.getTelNo(),
                detail.getBrand(),
                detail.getRouteCode(),
                detail.getSvarAddr(),
                detail.getConvenience(),
                detail.getMaintenanceYn(),
                detail.getTruckSaYn(),
                restStop.isAdminOverridden() || detail.isAdminOverridden());
    }
}
