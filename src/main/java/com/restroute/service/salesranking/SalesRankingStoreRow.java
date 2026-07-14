package com.restroute.service.salesranking;

public record SalesRankingStoreRow(
        String baseYearMonth,
        String overallRank,
        String restStopRank,
        String sourceRestStopCode,
        String sourceRestStopName,
        String sourceStoreCode,
        String sourceStoreName) {}
