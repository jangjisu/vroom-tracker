package com.restroute.service.evcharger;

import com.restroute.domain.EvChargerEntity;
import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.EvChargerRepository;
import com.restroute.repository.EvChargerStationMappingRepository;
import com.restroute.repository.RestStopRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    public EvChargerBackfillResult backfill() {
        List<RestStopEntity> restStops = restStopRepository.findAll();
        List<EvChargerEntity> stations = distinctActiveStations();
        List<String> statIds = stations.stream().map(EvChargerEntity::getStatId).toList();
        List<EvChargerStationMappingEntity> mappings =
                statIds.isEmpty() ? List.of() : mappingRepository.findAllByStatIdIn(statIds);

        int matchedCount = 0;
        List<EvChargerStationMappingEntity> toSave = new ArrayList<>();
        List<String> matchedStatIds = new ArrayList<>();
        for (EvChargerEntity station : stations) {
            EvChargerMatchResult result = findMatch(station, restStops);
            if (result.serviceAreaCode() == null) {
                continue;
            }
            EvChargerStationMappingEntity mapping = mappings.stream()
                    .filter(existing -> existing.getStatId().equals(station.getStatId()))
                    .findFirst()
                    .orElseGet(() -> EvChargerStationMappingEntity.of(station.getStatId()));
            mapping.updateMatch(result.serviceAreaCode(), result.distanceMeters(), result.matchType());
            toSave.add(mapping);
            matchedStatIds.add(station.getStatId());
            matchedCount++;
        }
        deleteStaleMappings(matchedStatIds);
        mappingRepository.saveAll(toSave);

        EvChargerBackfillResult result =
                EvChargerBackfillResult.of(statIds.size(), matchedCount, statIds.size() - matchedCount);
        log.info(
                "EV charger rest stop backfill completed. stationCount={}, matchedCount={}, unmatchedCount={}",
                result.stationCount(),
                result.matchedCount(),
                result.unmatchedCount());
        return result;
    }

    private void deleteStaleMappings(List<String> matchedStatIds) {
        if (matchedStatIds.isEmpty()) {
            mappingRepository.deleteAll();
            return;
        }
        mappingRepository.deleteAllByStatIdNotIn(matchedStatIds);
    }

    private List<EvChargerEntity> distinctActiveStations() {
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
