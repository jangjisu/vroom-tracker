package com.restroute.service.evcharger;

import com.restroute.domain.EvChargerEntity;
import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.EvChargerRepository;
import com.restroute.repository.EvChargerStationMappingRepository;
import com.restroute.repository.RestStopRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
public class EvChargerRestStopBackfillService {

    private static final double MAX_MATCH_DISTANCE_METERS = 300;
    private static final String MATCH_COORDINATE_AND_NAME = "COORDINATE_AND_NAME";
    private static final String MATCH_COORDINATE = "COORDINATE";
    private static final String MATCH_AMBIGUOUS = "AMBIGUOUS";
    private static final String MATCH_NO_COORDINATE = "NO_COORDINATE";

    private final EvChargerRepository evChargerRepository;
    private final EvChargerStationMappingRepository mappingRepository;
    private final RestStopRepository restStopRepository;

    @Transactional
    public Map<String, Integer> backfill() {
        List<RestStopEntity> restStops = restStopRepository.findAll();
        Map<String, EvChargerEntity> stations = evChargerRepository.findAll().stream()
                .filter(station -> StringUtils.hasText(station.getStatId()))
                .collect(Collectors.toMap(EvChargerEntity::getStatId, Function.identity(), (first, second) -> first));
        Map<String, EvChargerStationMappingEntity> mappings = new HashMap<>(mappingRepository.findAllByStatIdMap());

        int matchedCount = 0;
        int unmatchedCount = 0;
        List<EvChargerStationMappingEntity> toSave = new ArrayList<>();
        for (EvChargerEntity station : stations.values()) {
            EvChargerMatchResult result = findMatch(station, restStops);
            EvChargerStationMappingEntity mapping = mappings.get(station.getStatId());
            if (mapping == null) {
                mapping = EvChargerStationMappingEntity.of(station.getStatId());
                mappings.put(station.getStatId(), mapping);
            }
            mapping.updateMatch(result.serviceAreaCode(), result.distanceMeters(), result.matchType());
            toSave.add(mapping);
            if (result.serviceAreaCode() == null) {
                unmatchedCount++;
                continue;
            }
            matchedCount++;
        }
        mappingRepository.saveAll(toSave);

        Map<String, Integer> result = Map.of(
                "stationCount", stations.size(),
                "matchedCount", matchedCount,
                "unmatchedCount", unmatchedCount);
        log.info(
                "EV charger rest stop backfill completed. stationCount={}, matchedCount={}, unmatchedCount={}",
                result.get("stationCount"),
                result.get("matchedCount"),
                result.get("unmatchedCount"));
        return result;
    }

    private EvChargerMatchResult findMatch(EvChargerEntity station, List<RestStopEntity> restStops) {
        Optional<EvChargerCoordinates> stationCoordinates = coordinates(station.getLat(), station.getLng());
        if (stationCoordinates.isEmpty()) {
            return EvChargerMatchResult.unmatched(MATCH_NO_COORDINATE);
        }

        List<EvChargerDistanceCandidate> candidates = restStops.stream()
                .map(restStop -> distanceCandidate(restStop, stationCoordinates.get()))
                .flatMap(Optional::stream)
                .filter(candidate -> candidate.distanceMeters() <= MAX_MATCH_DISTANCE_METERS)
                .sorted(Comparator.comparing(EvChargerDistanceCandidate::distanceMeters))
                .toList();
        if (candidates.isEmpty()) {
            return EvChargerMatchResult.unmatched(MATCH_NO_COORDINATE);
        }

        List<EvChargerDistanceCandidate> nameMatches = candidates.stream()
                .filter(candidate ->
                        normalizedName(candidate.restStop().getUnitName()).equals(normalizedName(station.getStatNm())))
                .toList();
        if (nameMatches.size() == 1) {
            EvChargerDistanceCandidate candidate = nameMatches.get(0);
            return EvChargerMatchResult.matched(
                    candidate.restStop().getServiceAreaCode(), candidate.distanceMeters(), MATCH_COORDINATE_AND_NAME);
        }
        if (candidates.size() == 1) {
            EvChargerDistanceCandidate candidate = candidates.get(0);
            return EvChargerMatchResult.matched(
                    candidate.restStop().getServiceAreaCode(), candidate.distanceMeters(), MATCH_COORDINATE);
        }
        return EvChargerMatchResult.unmatched(MATCH_AMBIGUOUS);
    }

    private Optional<EvChargerDistanceCandidate> distanceCandidate(
            RestStopEntity restStop, EvChargerCoordinates stationCoordinates) {
        Optional<EvChargerCoordinates> restStopCoordinates = coordinates(restStop.getYValue(), restStop.getXValue());
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

    private Optional<EvChargerCoordinates> coordinates(String latitude, String longitude) {
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

    static String normalizedName(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", "").replace("휴게소", "").replaceAll("[^\\p{L}\\p{N}()]", "");
    }
}
