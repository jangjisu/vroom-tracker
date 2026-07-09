package com.restroute.service;

import com.restroute.domain.RestOilEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RestOilRepository;
import com.restroute.repository.RestStopRepository;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RestStopServiceAreaCodeMappingService {

    private final RestStopRepository restStopRepository;
    private final RestOilRepository restOilRepository;

    @Transactional(readOnly = true)
    public Map<String, String> mapByServiceAreaCode() {
        return restStopRepository.findAll().stream()
                .map(RestStopEntity::getServiceAreaCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (first, second) -> first));
    }

    @Transactional(readOnly = true)
    public Map<String, String> mapByStdRestCd() {
        return restStopRepository.findAll().stream()
                .filter(restStop -> StringUtils.hasText(restStop.getStdRestCd()))
                .filter(restStop -> StringUtils.hasText(restStop.getServiceAreaCode()))
                .collect(Collectors.toMap(
                        RestStopEntity::getStdRestCd, RestStopEntity::getServiceAreaCode, (first, second) -> first));
    }

    @Transactional(readOnly = true)
    public Map<String, String> mapByOilRestStopKey() {
        return restStopRepository.findAll().stream()
                .filter(restStop -> StringUtils.hasText(restStop.getRouteNo()))
                .filter(restStop -> StringUtils.hasText(restStop.getUnitName()))
                .filter(restStop -> StringUtils.hasText(restStop.getServiceAreaCode()))
                .collect(Collectors.toMap(
                        restStop -> oilRestStopKey(
                                restStop.getRouteNo(), RestOilEntity.normalizeStationName(restStop.getUnitName())),
                        RestStopEntity::getServiceAreaCode,
                        (first, second) -> first));
    }

    @Transactional(readOnly = true)
    public Map<String, String> mapByOilStandardRestCode() {
        return restOilRepository.findAll().stream()
                .filter(restOil -> StringUtils.hasText(restOil.getStandardRestCode()))
                .filter(restOil -> StringUtils.hasText(restOil.getRestStopServiceAreaCode()))
                .collect(Collectors.toMap(
                        RestOilEntity::getStandardRestCode,
                        RestOilEntity::getRestStopServiceAreaCode,
                        (first, second) -> first));
    }

    public static String oilRestStopKey(String routeCode, String normalizedStationName) {
        return routeCode + "\n" + normalizedStationName;
    }
}
