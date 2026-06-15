package com.vroomtracker.controller.response;

import com.vroomtracker.domain.RestOilEntity;
import com.vroomtracker.domain.RestOilPriceEntity;
import java.util.List;
import java.util.Optional;

public record OilInfoResponse(
        String oilCompany,
        String gasolinePrice,
        String dieselPrice,
        String lpgPrice,
        String telNo,
        List<OilStationConvenienceResponse> oilStationConveniences) {

    public static OilInfoResponse from(
            Optional<RestOilPriceEntity> oilPrice, List<RestOilEntity> oilStationConveniences) {
        return new OilInfoResponse(
                oilPrice.map(RestOilPriceEntity::getOilCompany).orElse(null),
                oilPrice.map(RestOilPriceEntity::getGasolinePrice).orElse(null),
                oilPrice.map(RestOilPriceEntity::getDieselPrice).orElse(null),
                oilPrice.map(RestOilPriceEntity::getLpgPrice).orElse(null),
                oilPrice.map(RestOilPriceEntity::getTelNo).orElse(null),
                oilStationConveniences.stream()
                        .map(OilStationConvenienceResponse::from)
                        .toList());
    }
}
