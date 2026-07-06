package com.restroute.controller.response;

import java.util.List;

public record RouteRestStopResponse(Destination destination, RouteSummary route, List<RouteRestStopItem> restStops) {

    public static RouteRestStopResponse of(
            Destination destination, RouteSummary route, List<RouteRestStopItem> restStops) {
        return new RouteRestStopResponse(destination, route, restStops);
    }

    public record Destination(String name, double latitude, double longitude) {

        public static Destination of(String name, double latitude, double longitude) {
            return new Destination(name, latitude, longitude);
        }
    }

    public record RouteSummary(long distanceMeters, long durationSeconds, List<List<Double>> path) {

        public static RouteSummary of(long distanceMeters, long durationSeconds, List<List<Double>> path) {
            return new RouteSummary(distanceMeters, durationSeconds, path);
        }
    }

    public record RouteRestStopItem(
            String serviceAreaCode,
            String unitName,
            String routeName,
            double latitude,
            double longitude,
            boolean hasDirectionAlternative,
            long distanceFromRouteMeters,
            ComparisonSummary comparisonSummary,
            List<RecommendationTag> recommendationTags) {

        public RouteRestStopItem(
                String serviceAreaCode,
                String unitName,
                String routeName,
                double latitude,
                double longitude,
                long distanceFromRouteMeters) {
            this(
                    serviceAreaCode,
                    unitName,
                    routeName,
                    latitude,
                    longitude,
                    false,
                    distanceFromRouteMeters,
                    ComparisonSummary.empty(),
                    List.of());
        }

        public static RouteRestStopItem of(
                String serviceAreaCode,
                String unitName,
                String routeName,
                double latitude,
                double longitude,
                long distanceFromRouteMeters) {
            return new RouteRestStopItem(
                    serviceAreaCode,
                    unitName,
                    routeName,
                    latitude,
                    longitude,
                    false,
                    distanceFromRouteMeters,
                    ComparisonSummary.empty(),
                    List.of());
        }

        public RouteRestStopItem withDirectionAlternative(boolean hasDirectionAlternative) {
            return new RouteRestStopItem(
                    serviceAreaCode,
                    unitName,
                    routeName,
                    latitude,
                    longitude,
                    hasDirectionAlternative,
                    distanceFromRouteMeters,
                    comparisonSummary,
                    recommendationTags);
        }

        public RouteRestStopItem withComparison(
                ComparisonSummary comparisonSummary, List<RecommendationTag> recommendationTags) {
            return new RouteRestStopItem(
                    serviceAreaCode,
                    unitName,
                    routeName,
                    latitude,
                    longitude,
                    hasDirectionAlternative,
                    distanceFromRouteMeters,
                    comparisonSummary,
                    List.copyOf(recommendationTags));
        }
    }

    public record ComparisonSummary(
            String gasolinePrice,
            String dieselPrice,
            String lpgPrice,
            Integer totalParkingCount,
            int foodMenuCount,
            int facilityCount) {

        public static ComparisonSummary empty() {
            return new ComparisonSummary(null, null, null, null, 0, 0);
        }

        public static ComparisonSummary of(
                String gasolinePrice,
                String dieselPrice,
                String lpgPrice,
                Integer totalParkingCount,
                int foodMenuCount,
                int facilityCount) {
            return new ComparisonSummary(
                    gasolinePrice, dieselPrice, lpgPrice, totalParkingCount, foodMenuCount, facilityCount);
        }
    }

    public record RecommendationTag(String key, String label) {

        public static RecommendationTag of(String key, String label) {
            return new RecommendationTag(key, label);
        }
    }
}
