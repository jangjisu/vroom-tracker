package com.vroomtracker.service;

import com.vroomtracker.controller.response.RestStopDetailViewResponse;
import com.vroomtracker.domain.HighwayServiceAreaInfoEntity;
import com.vroomtracker.domain.RestStopDetailEntity;
import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.HighwayServiceAreaInfoRepository;
import com.vroomtracker.repository.RestStopDetailRepository;
import com.vroomtracker.repository.RestStopRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestStopQueryService {

    private final RestStopRepository restStopRepository;
    private final RestStopDetailRepository restStopDetailRepository;
    private final HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;

    @Transactional(readOnly = true)
    public List<RestStopEntity> findAll() {
        return restStopRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<RestStopDetailViewResponse> findDetailByServiceAreaCode(String serviceAreaCode) {
        return restStopRepository
                .findByServiceAreaCode(serviceAreaCode)
                .map(restStop -> buildDetailResponse(restStop, serviceAreaCode));
    }

    private RestStopDetailViewResponse buildDetailResponse(RestStopEntity restStop, String serviceAreaCode) {
        List<RestStopDetailEntity> details = restStopDetailRepository.findAllByServiceAreaCode(serviceAreaCode);
        List<HighwayServiceAreaInfoEntity> infos =
                highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode(serviceAreaCode);

        String detailAddress = minText(details, RestStopDetailEntity::getSvarAddr);
        String fallbackAddress = minText(infos, HighwayServiceAreaInfoEntity::getServiceAreaAddress);

        return RestStopDetailViewResponse.of(
                restStop,
                firstNonNull(detailAddress, fallbackAddress),
                minText(details, RestStopDetailEntity::getConvenience),
                resolveYn(details, RestStopDetailEntity::getMaintenanceYn),
                resolveYn(details, RestStopDetailEntity::getTruckSaYn),
                minText(infos, HighwayServiceAreaInfoEntity::getDirectionTypeName),
                sumIntegerValues(infos, HighwayServiceAreaInfoEntity::getCompactCarParkingCount),
                sumIntegerValues(infos, HighwayServiceAreaInfoEntity::getFullSizeCarParkingCount),
                sumIntegerValues(infos, HighwayServiceAreaInfoEntity::getDisabledParkingCount));
    }

    private <T> String minText(List<T> items, Function<T, String> getter) {
        return items.stream()
                .map(getter)
                .filter(this::hasText)
                .map(String::trim)
                .min(String::compareTo)
                .orElse(null);
    }

    private String resolveYn(List<RestStopDetailEntity> details, Function<RestStopDetailEntity, String> getter) {
        if (details.stream().map(getter).anyMatch("O"::equals)) {
            return "O";
        }

        if (details.stream().map(getter).anyMatch("X"::equals)) {
            return "X";
        }

        return null;
    }

    private Integer sumIntegerValues(
            List<HighwayServiceAreaInfoEntity> infos, Function<HighwayServiceAreaInfoEntity, String> getter) {
        List<Integer> values = infos.stream()
                .map(getter)
                .map(this::parseInteger)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) {
            return null;
        }

        return values.stream().mapToInt(Integer::intValue).sum();
    }

    private Integer parseInteger(String value) {
        if (!hasText(value)) {
            return null;
        }

        return Integer.valueOf(value.trim());
    }

    private String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
