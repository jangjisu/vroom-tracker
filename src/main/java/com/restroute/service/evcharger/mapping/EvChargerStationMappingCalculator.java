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
            Optional<MatchedCandidate> matchedCandidate = findMatchedCandidate(charger, restStops, restStopDetails);
            if (matchedCandidate.isEmpty()) {
                continue;
            }
            mappings.add(createMapping(charger, matchedCandidate.get()));
        }
        return mappings;
    }

    private List<EvChargerEntity> distinctActiveChargers(List<EvChargerEntity> evChargers) {
        List<String> statIds = new ArrayList<>();
        List<EvChargerEntity> distinctChargers = new ArrayList<>();
        for (EvChargerEntity charger : evChargers) {
            if (StringUtils.hasText(charger.getStatId())
                    && "N".equals(charger.getDelYn())
                    && !statIds.contains(charger.getStatId())) {
                statIds.add(charger.getStatId());
                distinctChargers.add(charger);
            }
        }
        return distinctChargers;
    }

    private Optional<MatchedCandidate> findMatchedCandidate(
            EvChargerEntity charger, List<RestStopEntity> restStops, List<RestStopDetailEntity> restStopDetails) {
        List<DistanceCandidate> nearbyCandidates = findNearbyRestStops(charger, restStops);
        List<MatchedCandidate> matchedCandidates = nearbyCandidates.stream()
                .map(candidate -> matchCandidate(charger, candidate, restStopDetails))
                .flatMap(Optional::stream)
                .toList();
        List<MatchedCandidate> nameAndAddressCandidates = new ArrayList<>();
        List<MatchedCandidate> nameCandidates = new ArrayList<>();
        List<MatchedCandidate> addressCandidates = new ArrayList<>();
        for (MatchedCandidate candidate : matchedCandidates) {
            if (candidate.matchType() == EvChargerMatchType.NAME_ADDRESS_DISTANCE) {
                nameAndAddressCandidates.add(candidate);
                continue;
            }
            if (candidate.matchType() == EvChargerMatchType.NAME_DISTANCE) {
                nameCandidates.add(candidate);
                continue;
            }
            addressCandidates.add(candidate);
        }
        if (nameAndAddressCandidates.size() == 1) {
            return Optional.of(nameAndAddressCandidates.get(0));
        }
        if (!nameAndAddressCandidates.isEmpty()) {
            return Optional.empty();
        }

        if (nameCandidates.size() == 1) {
            return Optional.of(nameCandidates.get(0));
        }
        if (!nameCandidates.isEmpty()) {
            return Optional.empty();
        }

        if (addressCandidates.size() == 1) {
            return Optional.of(addressCandidates.get(0));
        }
        return Optional.empty();
    }

    private List<DistanceCandidate> findNearbyRestStops(
            EvChargerEntity charger, List<RestStopEntity> restStops) {
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

    private Optional<MatchedCandidate> matchCandidate(
            EvChargerEntity charger,
            DistanceCandidate distanceCandidate,
            List<RestStopDetailEntity> restStopDetails) {
        RestStopEntity restStop = distanceCandidate.restStop();
        List<RestStopDetailEntity> details = restStopDetails.stream()
                .filter(detail -> belongsToRestStop(detail, restStop))
                .toList();
        boolean nameMatched = isNameMatched(charger, restStop, details);
        boolean addressMatched = isAddressMatched(charger, details);

        if (nameMatched && addressMatched) {
            return Optional.of(new MatchedCandidate(distanceCandidate, EvChargerMatchType.NAME_ADDRESS_DISTANCE));
        }
        if (nameMatched) {
            return Optional.of(new MatchedCandidate(distanceCandidate, EvChargerMatchType.NAME_DISTANCE));
        }
        if (addressMatched) {
            return Optional.of(new MatchedCandidate(distanceCandidate, EvChargerMatchType.ADDRESS_DISTANCE));
        }
        return Optional.empty();
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

    private EvChargerStationMappingEntity createMapping(
            EvChargerEntity charger, MatchedCandidate matchedCandidate) {
        DistanceCandidate distanceCandidate = matchedCandidate.distanceCandidate();
        EvChargerStationMappingEntity mapping = EvChargerStationMappingEntity.of(charger.getStatId());
        mapping.updateMatch(
                distanceCandidate.restStop().getServiceAreaCode(),
                distanceCandidate.distanceMeters(),
                matchedCandidate.matchType());
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
            return Optional.of(EvChargerCoordinates.of(
                    Double.parseDouble(latitude.trim()), Double.parseDouble(longitude.trim())));
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

    private record MatchedCandidate(DistanceCandidate distanceCandidate, EvChargerMatchType matchType) {}
}
