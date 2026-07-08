package com.restroute.service.route;

record RouteRestStopRecommendationStandards(
        Integer lowestGasolinePrice, Integer lowestDieselPrice, Integer lowestLpgPrice, Integer largestParkingCount) {

    static RouteRestStopRecommendationStandards of(
            Integer lowestGasolinePrice,
            Integer lowestDieselPrice,
            Integer lowestLpgPrice,
            Integer largestParkingCount) {
        return new RouteRestStopRecommendationStandards(
                lowestGasolinePrice, lowestDieselPrice, lowestLpgPrice, largestParkingCount);
    }
}
