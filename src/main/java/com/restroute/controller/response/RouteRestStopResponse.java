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
            List<RecommendationTag> recommendationTags,
            RepresentativeFood representativeFood) {

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
                    List.of(),
                    null);
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
                    List.of(),
                    null);
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
                    recommendationTags,
                    representativeFood);
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
                    List.copyOf(recommendationTags),
                    representativeFood);
        }

        public RouteRestStopItem withRepresentativeFood(RepresentativeFood representativeFood) {
            return new RouteRestStopItem(
                    serviceAreaCode,
                    unitName,
                    routeName,
                    latitude,
                    longitude,
                    hasDirectionAlternative,
                    distanceFromRouteMeters,
                    comparisonSummary,
                    recommendationTags,
                    representativeFood);
        }
    }

    public record RepresentativeFood(String name, String price) {

        public static RepresentativeFood of(String name, String price) {
            return new RepresentativeFood(name, price);
        }
    }

    public record ComparisonSummary(
            String gasolinePrice,
            String dieselPrice,
            String lpgPrice,
            Integer gasolinePriceDiffFromAverage,
            Integer dieselPriceDiffFromAverage,
            Integer lpgPriceDiffFromAverage,
            Integer totalParkingCount,
            int foodMenuCount,
            int facilityCount) {

        public static ComparisonSummary empty() {
            return new ComparisonSummary(null, null, null, null, null, null, null, 0, 0);
        }

        public static ComparisonSummary of(
                String gasolinePrice,
                String dieselPrice,
                String lpgPrice,
                Integer gasolinePriceDiffFromAverage,
                Integer dieselPriceDiffFromAverage,
                Integer lpgPriceDiffFromAverage,
                Integer totalParkingCount,
                int foodMenuCount,
                int facilityCount) {
            return new ComparisonSummary(
                    gasolinePrice,
                    dieselPrice,
                    lpgPrice,
                    gasolinePriceDiffFromAverage,
                    dieselPriceDiffFromAverage,
                    lpgPriceDiffFromAverage,
                    totalParkingCount,
                    foodMenuCount,
                    facilityCount);
        }
    }

    public record NationalOilPriceSummary(
            String tradeDate, AverageOilPrice gasoline, AverageOilPrice diesel, AverageOilPrice lpg) {

        public static NationalOilPriceSummary of(
                String tradeDate, AverageOilPrice gasoline, AverageOilPrice diesel, AverageOilPrice lpg) {
            return new NationalOilPriceSummary(tradeDate, gasoline, diesel, lpg);
        }
    }

    public record AverageOilPrice(String productCode, String productName, String price, String dailyDiff) {

        public static AverageOilPrice of(String productCode, String productName, String price, String dailyDiff) {
            return new AverageOilPrice(productCode, productName, price, dailyDiff);
        }
    }

    public record RecommendationTag(String key, String label) {

        public static RecommendationTag of(String key, String label) {
            return new RecommendationTag(key, label);
        }
    }
}
