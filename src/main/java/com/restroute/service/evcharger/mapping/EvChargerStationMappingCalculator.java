package com.restroute.service.evcharger.mapping;

import com.restroute.domain.EvChargerEntity;
import com.restroute.domain.EvChargerStationMappingEntity;
import com.restroute.domain.RestStopDetailEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.service.evcharger.CoordinateDistanceCalculator;
import com.restroute.service.evcharger.EvChargerCoordinates;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EvChargerStationMappingCalculator {

    public static final double MAX_MATCH_DISTANCE_METERS = 300;

    public List<EvChargerStationMappingEntity> calculate(
            List<RestStopEntity> restStops,
            List<RestStopDetailEntity> restStopDetails,
            List<EvChargerEntity> evChargers) {
        List<EvChargerEntity> distinctChargers = distinctActiveChargers(evChargers);
        List<EvChargerStationMappingEntity> mappings = new ArrayList<>();

        for (EvChargerEntity charger : distinctChargers) {
            Optional<DistanceCandidate> matchedCandidate = findMatchedCandidate(charger, restStops, restStopDetails);
            if (matchedCandidate.isEmpty()) {
                continue;
            }
            mappings.add(createMapping(charger, matchedCandidate.get()));
        }
        return mappings;
    }

    private List<EvChargerEntity> distinctActiveChargers(List<EvChargerEntity> evChargers) {
        List<EvChargerEntity> distinctChargers = new ArrayList<>();
        evChargers.stream()
                .filter(EvChargerEntity::isActiveMappingTarget)
                .forEach(charger -> addIfNewStatId(distinctChargers, charger));
        return distinctChargers;
    }

    private void addIfNewStatId(List<EvChargerEntity> chargers, EvChargerEntity charger) {
        boolean alreadyAdded = chargers.stream().anyMatch(existing -> existing.hasSameStatId(charger));
        if (!alreadyAdded) {
            chargers.add(charger);
        }
    }

    private Optional<DistanceCandidate> findMatchedCandidate(
            EvChargerEntity charger, List<RestStopEntity> restStops, List<RestStopDetailEntity> restStopDetails) {
        return findNearbyRestStops(charger, restStops).stream()
                .filter(candidate -> isNameOrAddressMatched(charger, candidate.restStop(), restStopDetails))
                .findFirst();
    }

    private List<DistanceCandidate> findNearbyRestStops(EvChargerEntity charger, List<RestStopEntity> restStops) {
        Optional<EvChargerCoordinates> chargerCoordinates = parseCoordinates(charger.getLat(), charger.getLng());
        if (chargerCoordinates.isEmpty()) {
            return List.of();
        }

        return restStops.stream()
                .map(restStop -> calculateDistance(chargerCoordinates.get(), restStop))
                .flatMap(Optional::stream)
                .filter(candidate -> candidate.distanceMeters() <= MAX_MATCH_DISTANCE_METERS)
                .sorted(Comparator.comparing(DistanceCandidate::distanceMeters))
                .toList();
    }

    private Optional<DistanceCandidate> calculateDistance(
            EvChargerCoordinates chargerCoordinates, RestStopEntity restStop) {
        Optional<EvChargerCoordinates> restStopCoordinates =
                parseCoordinates(restStop.getYValue(), restStop.getXValue());
        if (restStopCoordinates.isEmpty()) {
            return Optional.empty();
        }

        EvChargerCoordinates coordinates = restStopCoordinates.get();
        double distanceMeters = CoordinateDistanceCalculator.meters(
                chargerCoordinates.latitude(),
                chargerCoordinates.longitude(),
                coordinates.latitude(),
                coordinates.longitude());
        return Optional.of(new DistanceCandidate(restStop, distanceMeters));
    }

    private boolean isNameOrAddressMatched(
            EvChargerEntity charger, RestStopEntity restStop, List<RestStopDetailEntity> restStopDetails) {
        List<RestStopDetailEntity> details = restStopDetails.stream()
                .filter(detail -> belongsToRestStop(detail, restStop))
                .toList();
        boolean nameMatched = isNameMatched(charger, restStop, details);
        boolean addressMatched = isAddressMatched(charger, details);

        return nameMatched || addressMatched;
    }

    private boolean isNameMatched(
            EvChargerEntity charger, RestStopEntity restStop, List<RestStopDetailEntity> restStopDetails) {
        if (sameNormalized(charger.getStatNm(), restStop.getUnitName())) {
            return true;
        }
        return restStopDetails.stream()
                .anyMatch(detail -> sameNormalized(charger.getStatNm(), detail.getServiceAreaName()));
    }

    private boolean isAddressMatched(EvChargerEntity charger, List<RestStopDetailEntity> restStopDetails) {
        String chargerAddress = normalizedAddress(charger.getAddr());
        if (!StringUtils.hasText(chargerAddress)) {
            return false;
        }
        return restStopDetails.stream()
                .map(RestStopDetailEntity::getSvarAddr)
                .map(this::normalizedAddress)
                .anyMatch(chargerAddress::equals);
    }

    private EvChargerStationMappingEntity createMapping(EvChargerEntity charger, DistanceCandidate distanceCandidate) {
        EvChargerStationMappingEntity mapping = EvChargerStationMappingEntity.of(charger.getStatId());
        mapping.updateMatch(distanceCandidate.restStop().getServiceAreaCode());
        return mapping;
    }

    private boolean belongsToRestStop(RestStopDetailEntity detail, RestStopEntity restStop) {
        if (StringUtils.hasText(detail.getRestStopServiceAreaCode())) {
            return detail.getRestStopServiceAreaCode().equals(restStop.getServiceAreaCode());
        }
        return StringUtils.hasText(detail.getServiceAreaCode())
                && detail.getServiceAreaCode().equals(restStop.getServiceAreaCode());
    }

    private Optional<EvChargerCoordinates> parseCoordinates(String latitude, String longitude) {
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

    private boolean sameNormalized(String first, String second) {
        String normalizedFirst = normalizeName(first);
        String normalizedSecond = normalizeName(second);
        return StringUtils.hasText(normalizedFirst)
                && StringUtils.hasText(normalizedSecond)
                && normalizedFirst.equals(normalizedSecond);
    }

    private String normalizeName(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", "").replace("휴게소", "").replaceAll("[^\\p{L}\\p{N}()]", "");
    }

    private String normalizedAddress(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", "").replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private record DistanceCandidate(RestStopEntity restStop, double distanceMeters) {}
}
