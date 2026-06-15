package com.vroomtracker.controller.response;

import com.vroomtracker.domain.RestOilEntity;

public record OilStationConvenienceResponse(String startTime, String endTime, String name, String description) {

    public static OilStationConvenienceResponse from(RestOilEntity entity) {
        return new OilStationConvenienceResponse(
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getConvenienceName(),
                entity.getConvenienceDescription());
    }
}
