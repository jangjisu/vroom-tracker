package com.restroute.controller.response;

import com.restroute.domain.RestStopStoreSalesRankEntity;

public record RestStopSalesRankingStoreItemResponse(int rank, String storeName) {

    public static RestStopSalesRankingStoreItemResponse from(RestStopStoreSalesRankEntity entity) {
        return new RestStopSalesRankingStoreItemResponse(
                Integer.parseInt(entity.getRestStopRank()), entity.getSourceStoreName());
    }
}
