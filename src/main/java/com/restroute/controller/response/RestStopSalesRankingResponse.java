package com.restroute.controller.response;

import com.restroute.domain.RestStopProductSalesRankEntity;
import java.util.List;

public record RestStopSalesRankingResponse(String baseYearMonth, List<RestStopSalesRankingItemResponse> products) {

    public static RestStopSalesRankingResponse of(String baseYearMonth, List<RestStopProductSalesRankEntity> products) {
        List<RestStopSalesRankingItemResponse> items =
                products.stream().map(RestStopSalesRankingItemResponse::from).toList();
        return new RestStopSalesRankingResponse(baseYearMonth, items);
    }

    public static RestStopSalesRankingResponse empty() {
        return new RestStopSalesRankingResponse(null, List.of());
    }
}
