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
        List<EvChargerEntity> distinctActiveChargers = distinctActiveChargers(evChargers);
        List<EvChargerStationMappingEntity> mappings = new ArrayList<>();

        for (EvChargerEntity charger : distinctActiveChargers) {
            Optional<MappingCandidate> candidate = findCandidate(charger, restStops, restStopDetails);
            if (candidate.isEmpty()) {
                continue;
            }
            MappingCandidate matched = candidate.get();
            EvChargerStationMappingEntity mapping = EvChargerStationMappingEntity.of(charger.getStatId());
            mapping.updateMatch(matched.restStop().getServiceAreaCode(), matched.distanceMeters(), matched.matchType());
            mappings.add(mapping);
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

    private Optional<MappingCandidate> findCandidate(
            EvChargerEntity charger, List<RestStopEntity> restStops, List<RestStopDetailEntity> restStopDetails) {
        Optional<EvChargerCoordinates> chargerCoordinates = coordinates(charger.getLat(), charger.getLng());
        if (chargerCoordinates.isEmpty()) {
            return Optional.empty();
        }

        List<MappingCandidate> candidates = restStops.stream()
                .map(restStop -> candidate(charger, chargerCoordinates.get(), restStop, restStopDetails))
                .flatMap(Optional::stream)
                .filter(candidate -> candidate.distanceMeters() <= MAX_MATCH_DISTANCE_METERS)
                .sorted(Comparator.comparing(MappingCandidate::distanceMeters))
                .toList();
        List<MappingCandidate> nameMatches =
                candidates.stream().filter(MappingCandidate::nameMatched).toList();
        List<MappingCandidate> addressMatches =
                candidates.stream().filter(MappingCandidate::addressMatched).toList();

        if (nameMatches.size() == 1) {
            MappingCandidate nameMatch = nameMatches.get(0);
            if (nameMatch.addressMatched()) {
                return Optional.of(nameMatch.withMatchType(EvChargerMatchType.NAME_ADDRESS_DISTANCE));
            }
            return Optional.of(nameMatch.withMatchType(EvChargerMatchType.NAME_DISTANCE));
        }
        if (addressMatches.size() == 1) {
            MappingCandidate addressMatch = addressMatches.get(0);
            if (addressMatch.nameMatched()) {
                return Optional.of(addressMatch.withMatchType(EvChargerMatchType.NAME_ADDRESS_DISTANCE));
            }
            return Optional.of(addressMatch.withMatchType(EvChargerMatchType.ADDRESS_DISTANCE));
        }
        return Optional.empty();
    }

    private Optional<MappingCandidate> candidate(
            EvChargerEntity charger,
            EvChargerCoordinates chargerCoordinates,
            RestStopEntity restStop,
            List<RestStopDetailEntity> restStopDetails) {
        Optional<EvChargerCoordinates> restStopCoordinates = coordinates(restStop.getYValue(), restStop.getXValue());
        if (restStopCoordinates.isEmpty()) {
            return Optional.empty();
        }
        EvChargerCoordinates coordinates = restStopCoordinates.get();
        double distanceMeters = CoordinateDistanceCalculator.meters(
                chargerCoordinates.latitude(),
                chargerCoordinates.longitude(),
                coordinates.latitude(),
                coordinates.longitude());
        List<RestStopDetailEntity> details = restStopDetails.stream()
                .filter(detail -> belongsToRestStop(detail, restStop))
                .toList();
        boolean nameMatched = sameNormalized(charger.getStatNm(), restStop.getUnitName())
                || details.stream()
                        .anyMatch(detail ->
                                sameNormalized(charger.getStatNm(), detail.getServiceAreaName()));
        boolean addressMatched = details.stream()
                .map(RestStopDetailEntity::getSvarAddr)
                .map(this::normalizedAddress)
                .anyMatch(address -> StringUtils.hasText(address)
                        && address.equals(normalizedAddress(charger.getAddr())));
        return Optional.of(new MappingCandidate(restStop, distanceMeters, nameMatched, addressMatched, null));
    }

    private boolean belongsToRestStop(RestStopDetailEntity detail, RestStopEntity restStop) {
        if (StringUtils.hasText(detail.getRestStopServiceAreaCode())) {
            return detail.getRestStopServiceAreaCode().equals(restStop.getServiceAreaCode());
        }
        return StringUtils.hasText(detail.getServiceAreaCode())
                && detail.getServiceAreaCode().equals(restStop.getServiceAreaCode());
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

    private String normalized(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", "").replace("휴게소", "").replaceAll("[^\\p{L}\\p{N}()]", "");
    }

    private boolean sameNormalized(String first, String second) {
        String normalizedFirst = normalized(first);
        String normalizedSecond = normalized(second);
        return StringUtils.hasText(normalizedFirst)
                && StringUtils.hasText(normalizedSecond)
                && normalizedFirst.equals(normalizedSecond);
    }

    private String normalizedAddress(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", "").replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private record MappingCandidate(
            RestStopEntity restStop,
            double distanceMeters,
            boolean nameMatched,
            boolean addressMatched,
            EvChargerMatchType matchType) {

        private MappingCandidate withMatchType(EvChargerMatchType matchType) {
            return new MappingCandidate(restStop, distanceMeters, nameMatched, addressMatched, matchType);
        }
    }
}
