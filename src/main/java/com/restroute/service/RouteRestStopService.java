package com.restroute.service;

import com.restroute.client.KakaoMapClient;
import com.restroute.client.response.KakaoDirectionsResponse;
import com.restroute.client.response.KakaoLocalSearchResponse;
import com.restroute.controller.response.RouteRestStopResponse;
import com.restroute.controller.response.RouteRestStopResponse.ComparisonSummary;
import com.restroute.controller.response.RouteRestStopResponse.Destination;
import com.restroute.controller.response.RouteRestStopResponse.RecommendationTag;
import com.restroute.controller.response.RouteRestStopResponse.RouteRestStopItem;
import com.restroute.controller.response.RouteRestStopResponse.RouteSummary;
import com.restroute.domain.HighwayServiceAreaInfoEntity;
import com.restroute.domain.RestOilEntity;
import com.restroute.domain.RestOilPriceEntity;
import com.restroute.domain.RestStopDetailEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.HighwayServiceAreaInfoRepository;
import com.restroute.repository.RestFoodRepository;
import com.restroute.repository.RestOilPriceRepository;
import com.restroute.repository.RestOilRepository;
import com.restroute.repository.RestStopDetailRepository;
import com.restroute.repository.RestStopRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RouteRestStopService {

    private static final int MAX_POLYLINE_POINTS = 300;

    private final KakaoMapClient kakaoMapClient;
    private final RestStopRepository restStopRepository;
    private final RestStopDetailRepository restStopDetailRepository;
    private final HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;
    private final RestOilRepository restOilRepository;
    private final RestOilPriceRepository restOilPriceRepository;
    private final RestFoodRepository restFoodRepository;

    public RouteRestStopResponse findRouteRestStops(
            double originLatitude,
            double originLongitude,
            String destinationQuery,
            Double destinationLatitude,
            Double destinationLongitude,
            String destinationName,
            int radiusMeters) {
        Destination destination =
                resolveDestination(destinationQuery, destinationLatitude, destinationLongitude, destinationName);

        KakaoDirectionsResponse directions = kakaoMapClient.getDirections(
                coordinateParam(originLongitude, originLatitude),
                coordinateParam(destination.longitude(), destination.latitude()));
        if (!directions.hasSuccessfulRoute()) {
            KakaoDirectionsResponse.Route failedRoute = directions.firstRoute();
            throw new RouteRestStopNotFoundException(
                    routeFailureMessage(failedRoute == null ? null : failedRoute.resultCode()));
        }

        KakaoDirectionsResponse.Route route = directions.firstRoute();
        RoutePolyline polyline = RoutePolyline.fromRoute(route).downsample(MAX_POLYLINE_POINTS);
        if (polyline.isEmpty()) {
            throw new RouteRestStopNotFoundException("경로 좌표가 없습니다.");
        }

        List<RouteRestStopItem> restStops = restStopsOnRoute(polyline, radiusMeters);
        return RouteRestStopResponse.of(destination, routeSummary(route, polyline), restStops);
    }

    private String routeFailureMessage(Integer resultCode) {
        int code = resultCode == null ? -1 : resultCode;
        return switch (code) {
            case 101, 105 -> "출발지 주변에서 도로를 찾지 못했어요. 출발지를 도로에 가까운 위치로 바꿔주세요.";
            case 102, 106 -> "도착지 주변에서 도로를 찾지 못했어요. 도착지를 도로에 가까운 위치로 바꿔주세요.";
            case 104 -> "출발지와 도착지가 너무 가까워요. 좀 더 떨어진 위치를 선택해주세요.";
            default -> "경로를 찾지 못했어요. 출발지와 도착지를 다시 확인해주세요.";
        };
    }

    private Destination resolveDestination(
            String destinationQuery, Double destinationLatitude, Double destinationLongitude, String destinationName) {
        if (destinationLatitude != null && destinationLongitude != null) {
            String name = destinationName == null || destinationName.isBlank() ? "목적지" : destinationName;
            return Destination.of(name, destinationLatitude, destinationLongitude);
        }

        KakaoLocalSearchResponse search = kakaoMapClient.searchKeyword(destinationQuery);
        if (search.isEmpty()) {
            throw new RouteRestStopNotFoundException("목적지 검색 결과가 없습니다: " + destinationQuery);
        }

        KakaoLocalSearchResponse.Document document = search.first();
        Double longitude = parseCoordinate(document.x());
        Double latitude = parseCoordinate(document.y());
        if (longitude == null || latitude == null) {
            throw new RouteRestStopNotFoundException("목적지 좌표를 해석하지 못했습니다.");
        }

        return Destination.of(document.label(), latitude, longitude);
    }

    private List<RouteRestStopItem> restStopsOnRoute(RoutePolyline polyline, int radiusMeters) {
        List<RouteRestStopCandidate> candidates = new ArrayList<>();
        for (RestStopEntity restStop : restStopRepository.findAll()) {
            Double latitude = parseCoordinate(restStop.getYValue());
            Double longitude = parseCoordinate(restStop.getXValue());
            if (latitude == null || longitude == null) {
                continue;
            }

            RoutePolyline.Nearest nearest = polyline.nearest(latitude, longitude);
            if (nearest.distanceMeters() > radiusMeters) {
                continue;
            }

            RouteRestStopItem item = RouteRestStopItem.of(
                    restStop.getServiceAreaCode(),
                    restStop.getUnitName(),
                    restStop.getRouteName(),
                    latitude,
                    longitude,
                    Math.round(nearest.distanceMeters()));
            candidates.add(RouteRestStopCandidate.of(restStop, item, nearest.index()));
        }

        Map<String, Long> groupCounts = candidates.stream()
                .filter(RouteRestStopCandidate::hasDirectionGroup)
                .map(RouteRestStopCandidate::groupKey)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<RouteRestStopComparison> comparisons = candidates.stream()
                .map(candidate -> RouteRestStopComparison.of(candidate, comparisonOf(candidate.restStop())))
                .toList();
        Integer lowestGasolinePrice = lowestPrice(comparisons, FuelType.GASOLINE);
        Integer lowestDieselPrice = lowestPrice(comparisons, FuelType.DIESEL);
        Integer lowestLpgPrice = lowestPrice(comparisons, FuelType.LPG);
        Integer largestParkingCount = largestParkingCount(comparisons);
        return comparisons.stream()
                .sorted(Comparator.comparingInt(
                        comparison -> comparison.candidate().routeIndex()))
                .map(comparison -> comparison
                        .candidate()
                        .item()
                        .withDirectionAlternative(
                                groupCounts.getOrDefault(comparison.candidate().groupKey(), 0L) > 1)
                        .withComparison(
                                comparison.summary(),
                                recommendationTags(
                                        comparison,
                                        lowestGasolinePrice,
                                        lowestDieselPrice,
                                        lowestLpgPrice,
                                        largestParkingCount)))
                .toList();
    }

    private ComparisonSummary comparisonOf(RestStopEntity restStop) {
        Optional<RestStopDetailEntity> detail =
                restStopDetailRepository.findByServiceAreaCode(restStop.getServiceAreaCode());
        List<HighwayServiceAreaInfoEntity> infos =
                highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode(restStop.getServiceAreaCode());
        List<RestOilEntity> oilConveniences = restOilRepository.findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc(
                restStop.getRouteNo(), RestOilEntity.normalizeStationName(restStop.getUnitName()));
        Optional<RestOilPriceEntity> oilPrice = findOilPrice(oilConveniences);
        int foodMenuCount = restFoodRepository
                .findAllByStdRestCdOrderByIdAsc(restStop.getStdRestCd())
                .size();
        return ComparisonSummary.of(
                oilPrice.map(RestOilPriceEntity::getGasolinePrice).orElse(null),
                oilPrice.map(RestOilPriceEntity::getDieselPrice).orElse(null),
                oilPrice.map(RestOilPriceEntity::getLpgPrice).orElse(null),
                totalParkingCount(infos),
                foodMenuCount,
                facilityCount(detail, oilConveniences));
    }

    private Optional<RestOilPriceEntity> findOilPrice(List<RestOilEntity> oilConveniences) {
        return oilConveniences.stream()
                .map(RestOilEntity::getStandardRestCode)
                .filter(StringUtils::hasText)
                .findFirst()
                .flatMap(restOilPriceRepository::findByServiceAreaCode2);
    }

    private Integer totalParkingCount(List<HighwayServiceAreaInfoEntity> infos) {
        int total = infos.stream()
                .mapToInt(info -> parseCount(info.getCompactCarParkingCount())
                        + parseCount(info.getFullSizeCarParkingCount())
                        + parseCount(info.getDisabledParkingCount()))
                .sum();
        if (total == 0) {
            return null;
        }
        return total;
    }

    private int facilityCount(Optional<RestStopDetailEntity> detail, List<RestOilEntity> oilConveniences) {
        long detailConvenienceCount = detail.stream()
                .map(RestStopDetailEntity::getConvenience)
                .filter(StringUtils::hasText)
                .flatMap(convenience -> List.of(convenience.split("[,/]")).stream())
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .count();
        long detailFlagCount = detail.stream()
                .mapToLong(restStopDetail ->
                        ynCount(restStopDetail.getMaintenanceYn()) + ynCount(restStopDetail.getTruckSaYn()))
                .sum();
        long oilConvenienceCount = oilConveniences.stream()
                .map(RestOilEntity::getConvenienceName)
                .filter(StringUtils::hasText)
                .distinct()
                .count();
        return Math.toIntExact(detailConvenienceCount + detailFlagCount + oilConvenienceCount);
    }

    private int ynCount(String value) {
        if ("Y".equals(value)) {
            return 1;
        }
        return 0;
    }

    private Integer lowestPrice(List<RouteRestStopComparison> comparisons, FuelType fuelType) {
        return comparisons.stream()
                .map(comparison -> priceOf(comparison.summary(), fuelType))
                .flatMap(Optional::stream)
                .min(Integer::compareTo)
                .orElse(null);
    }

    private Integer largestParkingCount(List<RouteRestStopComparison> comparisons) {
        return comparisons.stream()
                .map(comparison -> comparison.summary().totalParkingCount())
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private List<RecommendationTag> recommendationTags(
            RouteRestStopComparison comparison,
            Integer lowestGasolinePrice,
            Integer lowestDieselPrice,
            Integer lowestLpgPrice,
            Integer largestParkingCount) {
        List<RecommendationTag> tags = new ArrayList<>();
        addLowestPriceTag(tags, comparison.summary(), FuelType.GASOLINE, lowestGasolinePrice);
        addLowestPriceTag(tags, comparison.summary(), FuelType.DIESEL, lowestDieselPrice);
        addLowestPriceTag(tags, comparison.summary(), FuelType.LPG, lowestLpgPrice);
        addLargestParkingTag(tags, comparison.summary(), largestParkingCount);
        addFoodTag(tags, comparison.summary());
        addFacilityTag(tags, comparison.summary());
        return tags;
    }

    private void addLowestPriceTag(
            List<RecommendationTag> tags, ComparisonSummary summary, FuelType fuelType, Integer lowestPrice) {
        Optional<Integer> price = priceOf(summary, fuelType);
        if (price.isEmpty()) {
            return;
        }
        if (!price.get().equals(lowestPrice)) {
            return;
        }
        tags.add(RecommendationTag.of(fuelType.tagKey(), fuelType.tagLabel()));
    }

    private void addLargestParkingTag(
            List<RecommendationTag> tags, ComparisonSummary summary, Integer largestParkingCount) {
        if (largestParkingCount == null || !largestParkingCount.equals(summary.totalParkingCount())) {
            return;
        }
        tags.add(RecommendationTag.of("largest-parking", "주차장 큼"));
    }

    private void addFoodTag(List<RecommendationTag> tags, ComparisonSummary summary) {
        if (summary.foodMenuCount() <= 0) {
            return;
        }
        tags.add(RecommendationTag.of("food-available", "먹거리 있음"));
    }

    private void addFacilityTag(List<RecommendationTag> tags, ComparisonSummary summary) {
        if (summary.facilityCount() < 3) {
            return;
        }
        tags.add(RecommendationTag.of("many-facilities", "시설 많음"));
    }

    private Optional<Integer> priceOf(ComparisonSummary summary, FuelType fuelType) {
        return switch (fuelType) {
            case GASOLINE -> parsePrice(summary.gasolinePrice());
            case DIESEL -> parsePrice(summary.dieselPrice());
            case LPG -> parsePrice(summary.lpgPrice());
        };
    }

    private Optional<Integer> parsePrice(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (!StringUtils.hasText(digits)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(digits));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private int parseCount(String value) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (!StringUtils.hasText(digits)) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private RouteSummary routeSummary(KakaoDirectionsResponse.Route route, RoutePolyline polyline) {
        long distance = summaryValue(route, true);
        long duration = summaryValue(route, false);
        List<List<Double>> path = polyline.coordinates().stream()
                .map(coordinate -> List.of(coordinate.longitude(), coordinate.latitude()))
                .toList();
        return RouteSummary.of(distance, duration, path);
    }

    private long summaryValue(KakaoDirectionsResponse.Route route, boolean distance) {
        KakaoDirectionsResponse.Summary summary = route.summary();
        if (summary == null) {
            return 0L;
        }
        Long value = distance ? summary.distance() : summary.duration();
        return value == null ? 0L : value;
    }

    private String coordinateParam(double longitude, double latitude) {
        return longitude + "," + latitude;
    }

    private Double parseCoordinate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private enum FuelType {
        GASOLINE("lowest-gasoline", "휘발유 최저가"),
        DIESEL("lowest-diesel", "경유 최저가"),
        LPG("lowest-lpg", "LPG 최저가");

        private final String tagKey;
        private final String tagLabel;

        FuelType(String tagKey, String tagLabel) {
            this.tagKey = tagKey;
            this.tagLabel = tagLabel;
        }

        private String tagKey() {
            return tagKey;
        }

        private String tagLabel() {
            return tagLabel;
        }
    }
}
