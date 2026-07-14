package com.restroute.service.admindashboard;

public record AdminDashboardSummary(long restStopCount, String latestSalesRankingMonth, String lastSyncStatus) {

    public static AdminDashboardSummary of(long restStopCount, String latestSalesRankingMonth, String lastSyncStatus) {
        return new AdminDashboardSummary(restStopCount, latestSalesRankingMonth, lastSyncStatus);
    }
}
