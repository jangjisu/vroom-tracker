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
            long distanceFromRouteMeters) {

        public RouteRestStopItem(
                String serviceAreaCode,
                String unitName,
                String routeName,
                double latitude,
                double longitude,
                long distanceFromRouteMeters) {
            this(serviceAreaCode, unitName, routeName, latitude, longitude, false, distanceFromRouteMeters);
        }

        public static RouteRestStopItem of(
                String serviceAreaCode,
                String unitName,
                String routeName,
                double latitude,
                double longitude,
                long distanceFromRouteMeters) {
            return new RouteRestStopItem(
                    serviceAreaCode, unitName, routeName, latitude, longitude, false, distanceFromRouteMeters);
        }

        public RouteRestStopItem withDirectionAlternative(boolean hasDirectionAlternative) {
            return new RouteRestStopItem(
                    serviceAreaCode,
                    unitName,
                    routeName,
                    latitude,
                    longitude,
                    hasDirectionAlternative,
                    distanceFromRouteMeters);
        }
    }
}
