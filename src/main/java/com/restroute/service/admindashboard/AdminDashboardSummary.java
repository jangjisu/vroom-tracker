package com.restroute.service.admindashboard;

public record AdminDashboardSummary(long restStopCount, String latestSalesRankingMonth) {

    public static AdminDashboardSummary of(long restStopCount, String latestSalesRankingMonth) {
        return new AdminDashboardSummary(restStopCount, latestSalesRankingMonth);
    }
}
