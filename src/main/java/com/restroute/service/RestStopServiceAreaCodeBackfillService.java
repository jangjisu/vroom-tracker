package com.restroute.service;

import com.restroute.domain.EvChargerEntity;
import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.domain.HighwayServiceAreaInfoEntity;
import com.restroute.domain.RestFoodEntity;
import com.restroute.domain.RestOilEntity;
import com.restroute.domain.RestOilPriceEntity;
import com.restroute.domain.RestStopDetailEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.EvChargerRepository;
import com.restroute.repository.EvChargerStationMappingRepository;
import com.restroute.repository.HighwayServiceAreaInfoRepository;
import com.restroute.repository.RestFoodRepository;
import com.restroute.repository.RestOilPriceRepository;
import com.restroute.repository.RestOilRepository;
import com.restroute.repository.RestStopDetailRepository;
import com.restroute.repository.RestStopRepository;
import com.restroute.service.evcharger.CoordinateDistanceCalculator;
import com.restroute.service.evcharger.EvChargerCoordinates;
import com.restroute.service.evcharger.EvChargerDistanceCandidate;
import com.restroute.service.evcharger.EvChargerMatchResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestStopServiceAreaCodeBackfillService {

    public static final String REST_STOP_DETAIL_MAPPED_COUNT = "restStopDetailMappedCount";
    public static final String HIGHWAY_SERVICE_AREA_INFO_MAPPED_COUNT = "highwayServiceAreaInfoMappedCount";
    public static final String REST_FOOD_MAPPED_COUNT = "restFoodMappedCount";
    public static final String REST_OIL_MAPPED_COUNT = "restOilMappedCount";
    public static final String REST_OIL_PRICE_MAPPED_COUNT = "restOilPriceMappedCount";
    public static final String EV_CHARGER_MAPPED_COUNT = "evChargerMappedCount";

    private static final double MAX_EV_MATCH_DISTANCE_METERS = 300;
    private static final String EV_MATCH_COORDINATE_AND_NAME = "COORDINATE_AND_NAME";
    private static final String EV_MATCH_COORDINATE = "COORDINATE";

    private final RestStopRepository restStopRepository;
    private final RestStopDetailRepository restStopDetailRepository;
    private final HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;
    private final RestFoodRepository restFoodRepository;
    private final RestOilRepository restOilRepository;
    private final RestOilPriceRepository restOilPriceRepository;
    private final EvChargerRepository evChargerRepository;
    private final EvChargerStationMappingRepository evChargerStationMappingRepository;

    @Transactional
    public Map<String, Integer> backfill() {
        List<RestStopEntity> restStops = restStopRepository.findAll();
        Map<String, String> serviceAreaCodeByServiceAreaCode = mapServiceAreaCode(restStops);
        Map<String, String> serviceAreaCodeByStdRestCd = mapByStdRestCd(restStops);
        Map<String, String> serviceAreaCodeByOilKey = mapByOilKey(restStops);

        int restStopDetailMappedCount = backfillRestStopDetails(serviceAreaCodeByServiceAreaCode);
        int highwayServiceAreaInfoMappedCount = backfillHighwayServiceAreaInfos(serviceAreaCodeByServiceAreaCode);
        int restFoodMappedCount = backfillRestFoods(serviceAreaCodeByStdRestCd);
        int restOilMappedCount = backfillRestOils(serviceAreaCodeByOilKey);
        int restOilPriceMappedCount = backfillRestOilPrices(mapByOilStandardRestCode());
        int evChargerMappedCount = backfillEvChargerMappings(restStops);

        Map<String, Integer> result = Map.of(
                REST_STOP_DETAIL_MAPPED_COUNT,
                restStopDetailMappedCount,
                HIGHWAY_SERVICE_AREA_INFO_MAPPED_COUNT,
                highwayServiceAreaInfoMappedCount,
                REST_FOOD_MAPPED_COUNT,
                restFoodMappedCount,
                REST_OIL_MAPPED_COUNT,
                restOilMappedCount,
                REST_OIL_PRICE_MAPPED_COUNT,
                restOilPriceMappedCount,
                EV_CHARGER_MAPPED_COUNT,
                evChargerMappedCount);
        log.info(
                "Rest stop service area code backfill completed. restStopDetailMappedCount={}, "
                        + "highwayServiceAreaInfoMappedCount={}, restFoodMappedCount={}, restOilMappedCount={}, "
                        + "restOilPriceMappedCount={}, evChargerMappedCount={}",
                result.get(REST_STOP_DETAIL_MAPPED_COUNT),
                result.get(HIGHWAY_SERVICE_AREA_INFO_MAPPED_COUNT),
                result.get(REST_FOOD_MAPPED_COUNT),
                result.get(REST_OIL_MAPPED_COUNT),
                result.get(REST_OIL_PRICE_MAPPED_COUNT),
                result.get(EV_CHARGER_MAPPED_COUNT));
        return result;
    }

    private int backfillEvChargerMappings(List<RestStopEntity> restStops) {
        List<EvChargerEntity> activeStations = distinctActiveEvStations();
        List<String> statIds =
                activeStations.stream().map(EvChargerEntity::getStatId).toList();
        List<EvChargerStationMappingEntity> existingMappings =
                statIds.isEmpty() ? List.of() : evChargerStationMappingRepository.findAllByStatIdIn(statIds);
        List<EvChargerStationMappingEntity> mappingsToSave = new ArrayList<>();
        List<String> matchedStatIds = new ArrayList<>();

        for (EvChargerEntity station : activeStations) {
            EvChargerMatchResult match = findEvChargerMatch(station, restStops);
            if (match.serviceAreaCode() == null) {
                continue;
            }
            EvChargerStationMappingEntity mapping = existingMappings.stream()
                    .filter(existing -> existing.getStatId().equals(station.getStatId()))
                    .findFirst()
                    .orElseGet(() -> EvChargerStationMappingEntity.of(station.getStatId()));
            mapping.updateMatch(match.serviceAreaCode(), match.distanceMeters(), match.matchType());
            mappingsToSave.add(mapping);
            matchedStatIds.add(station.getStatId());
        }

        if (matchedStatIds.isEmpty()) {
            evChargerStationMappingRepository.deleteAll();
        }
        if (!matchedStatIds.isEmpty()) {
            evChargerStationMappingRepository.deleteAllByStatIdNotIn(matchedStatIds);
        }
        evChargerStationMappingRepository.saveAll(mappingsToSave);
        return mappingsToSave.size();
    }

    private List<EvChargerEntity> distinctActiveEvStations() {
        List<String> statIds = new ArrayList<>();
        List<EvChargerEntity> stations = new ArrayList<>();
        for (EvChargerEntity charger : evChargerRepository.findAll()) {
            if (StringUtils.hasText(charger.getStatId())
                    && "N".equals(charger.getDelYn())
                    && !statIds.contains(charger.getStatId())) {
                statIds.add(charger.getStatId());
                stations.add(charger);
            }
        }
        return stations;
    }

    private EvChargerMatchResult findEvChargerMatch(EvChargerEntity station, List<RestStopEntity> restStops) {
        Optional<EvChargerCoordinates> stationCoordinates = evCoordinates(station.getLat(), station.getLng());
        if (stationCoordinates.isEmpty()) {
            return EvChargerMatchResult.unmatched(null);
        }
        List<EvChargerDistanceCandidate> candidates = restStops.stream()
                .map(restStop -> evDistanceCandidate(restStop, stationCoordinates.get()))
                .flatMap(Optional::stream)
                .filter(candidate -> candidate.distanceMeters() <= MAX_EV_MATCH_DISTANCE_METERS)
                .sorted(Comparator.comparing(EvChargerDistanceCandidate::distanceMeters))
                .toList();
        if (candidates.isEmpty()) {
            return EvChargerMatchResult.unmatched(null);
        }
        List<EvChargerDistanceCandidate> nameMatches = candidates.stream()
                .filter(candidate -> normalizedEvName(candidate.restStop().getUnitName())
                        .equals(normalizedEvName(station.getStatNm())))
                .toList();
        if (nameMatches.size() == 1) {
            EvChargerDistanceCandidate candidate = nameMatches.get(0);
            return EvChargerMatchResult.matched(
                    candidate.restStop().getServiceAreaCode(),
                    candidate.distanceMeters(),
                    EV_MATCH_COORDINATE_AND_NAME);
        }
        if (candidates.size() == 1) {
            EvChargerDistanceCandidate candidate = candidates.get(0);
            return EvChargerMatchResult.matched(
                    candidate.restStop().getServiceAreaCode(), candidate.distanceMeters(), EV_MATCH_COORDINATE);
        }
        return EvChargerMatchResult.unmatched(null);
    }

    private Optional<EvChargerDistanceCandidate> evDistanceCandidate(
            RestStopEntity restStop, EvChargerCoordinates stationCoordinates) {
        Optional<EvChargerCoordinates> restStopCoordinates = evCoordinates(restStop.getYValue(), restStop.getXValue());
        if (restStopCoordinates.isEmpty()) {
            return Optional.empty();
        }
        EvChargerCoordinates coordinates = restStopCoordinates.get();
        double distance = CoordinateDistanceCalculator.meters(
                stationCoordinates.latitude(),
                stationCoordinates.longitude(),
                coordinates.latitude(),
                coordinates.longitude());
        return Optional.of(EvChargerDistanceCandidate.of(restStop, distance));
    }

    private Optional<EvChargerCoordinates> evCoordinates(String latitude, String longitude) {
        if (!StringUtils.hasText(latitude) || !StringUtils.hasText(longitude)) {
            return Optional.empty();
        }
        try {
            return Optional.of(
                    EvChargerCoordinates.of(Double.parseDouble(latitude.trim()), Double.parseDouble(longitude.trim())));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String normalizedEvName(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", "").replace("휴게소", "").replaceAll("[^\\p{L}\\p{N}()]", "");
    }

    private Map<String, String> mapServiceAreaCode(List<RestStopEntity> restStops) {
        return restStops.stream()
                .map(RestStopEntity::getServiceAreaCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (first, second) -> first));
    }

    private Map<String, String> mapByStdRestCd(List<RestStopEntity> restStops) {
        return restStops.stream()
                .filter(restStop -> StringUtils.hasText(restStop.getStdRestCd()))
                .filter(restStop -> StringUtils.hasText(restStop.getServiceAreaCode()))
                .collect(Collectors.toMap(
                        RestStopEntity::getStdRestCd, RestStopEntity::getServiceAreaCode, (first, second) -> first));
    }

    private Map<String, String> mapByOilKey(List<RestStopEntity> restStops) {
        return restStops.stream()
                .filter(restStop -> StringUtils.hasText(restStop.getRouteNo()))
                .filter(restStop -> StringUtils.hasText(restStop.getUnitName()))
                .filter(restStop -> StringUtils.hasText(restStop.getServiceAreaCode()))
                .collect(Collectors.toMap(
                        restStop -> oilRestStopKey(
                                restStop.getRouteNo(), RestOilEntity.normalizeStationName(restStop.getUnitName())),
                        RestStopEntity::getServiceAreaCode,
                        (first, second) -> first));
    }

    private Map<String, String> mapByOilStandardRestCode() {
        return restOilRepository.findAll().stream()
                .filter(restOil -> StringUtils.hasText(restOil.getStandardRestCode()))
                .filter(restOil -> StringUtils.hasText(restOil.getRestStopServiceAreaCode()))
                .collect(Collectors.toMap(
                        RestOilEntity::getStandardRestCode,
                        RestOilEntity::getRestStopServiceAreaCode,
                        (first, second) -> first));
    }

    private int backfillRestStopDetails(Map<String, String> serviceAreaCodeByServiceAreaCode) {
        int mappedCount = 0;
        for (RestStopDetailEntity detail : restStopDetailRepository.findAll()) {
            String restStopServiceAreaCode = serviceAreaCodeByServiceAreaCode.get(detail.getServiceAreaCode());
            detail.updateRestStopServiceAreaCode(restStopServiceAreaCode);
            if (restStopServiceAreaCode != null) {
                mappedCount++;
            }
        }
        return mappedCount;
    }

    private int backfillHighwayServiceAreaInfos(Map<String, String> serviceAreaCodeByServiceAreaCode) {
        int mappedCount = 0;
        for (HighwayServiceAreaInfoEntity info : highwayServiceAreaInfoRepository.findAll()) {
            String restStopServiceAreaCode = serviceAreaCodeByServiceAreaCode.get(info.getBusinessFacilityCode());
            info.updateRestStopServiceAreaCode(restStopServiceAreaCode);
            if (restStopServiceAreaCode != null) {
                mappedCount++;
            }
        }
        return mappedCount;
    }

    private int backfillRestFoods(Map<String, String> serviceAreaCodeByStdRestCd) {
        int mappedCount = 0;
        for (RestFoodEntity food : restFoodRepository.findAll()) {
            String restStopServiceAreaCode = serviceAreaCodeByStdRestCd.get(food.getStdRestCd());
            food.updateRestStopServiceAreaCode(restStopServiceAreaCode);
            if (restStopServiceAreaCode != null) {
                mappedCount++;
            }
        }
        return mappedCount;
    }

    private int backfillRestOils(Map<String, String> serviceAreaCodeByOilKey) {
        int mappedCount = 0;
        for (RestOilEntity oil : restOilRepository.findAll()) {
            String key = oilRestStopKey(oil.getRouteCode(), oil.getNormalizedStationName());
            String restStopServiceAreaCode = serviceAreaCodeByOilKey.get(key);
            oil.updateRestStopServiceAreaCode(restStopServiceAreaCode);
            if (restStopServiceAreaCode != null) {
                mappedCount++;
            }
        }
        return mappedCount;
    }

    private int backfillRestOilPrices(Map<String, String> serviceAreaCodeByOilStandardRestCode) {
        int mappedCount = 0;
        for (RestOilPriceEntity oilPrice : restOilPriceRepository.findAll()) {
            String restStopServiceAreaCode = serviceAreaCodeByOilStandardRestCode.get(oilPrice.getServiceAreaCode2());
            oilPrice.updateRestStopServiceAreaCode(restStopServiceAreaCode);
            if (restStopServiceAreaCode != null) {
                mappedCount++;
            }
        }
        return mappedCount;
    }

    private String oilRestStopKey(String routeCode, String normalizedStationName) {
        return routeCode + "\n" + normalizedStationName;
    }
}
