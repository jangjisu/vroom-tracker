package com.restroute.service.salesranking;

public record SalesRankingUploadResult(String baseYearMonth, int productCount, int storeCount) {

    public static SalesRankingUploadResult of(String baseYearMonth, int productCount, int storeCount) {
        return new SalesRankingUploadResult(baseYearMonth, productCount, storeCount);
    }
}
