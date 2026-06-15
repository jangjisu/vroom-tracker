package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.response.RestOilPriceItem;
import com.vroomtracker.client.response.RestOilPriceResponse;
import com.vroomtracker.controller.response.OilInfoResponse;
import com.vroomtracker.domain.RestOilEntity;
import com.vroomtracker.domain.RestOilPriceEntity;
import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.RestOilPriceRepository;
import com.vroomtracker.repository.RestOilRepository;
import com.vroomtracker.repository.RestStopRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RestOilPriceRefreshService {

    private final RestStopRepository restStopRepository;
    private final RestOilRepository restOilRepository;
    private final RestOilPriceRepository restOilPriceRepository;
    private final ExApiClient exApiClient;
    private final TransactionTemplate transactionTemplate;

    public Optional<OilInfoResponse> refreshByServiceAreaCode(String serviceAreaCode) {
        return restStopRepository.findByServiceAreaCode(serviceAreaCode).flatMap(this::refreshByRestStop);
    }

    private Optional<OilInfoResponse> refreshByRestStop(RestStopEntity restStop) {
        List<RestOilEntity> conveniences = findOilStationConveniences(restStop);
        Optional<String> serviceAreaCode2 = firstServiceAreaCode2(conveniences);
        if (serviceAreaCode2.isEmpty()) {
            return Optional.empty();
        }

        Optional<RestOilPriceItem> fetchedItem = fetchOilPrice(serviceAreaCode2.get());
        if (fetchedItem.isEmpty()) {
            return Optional.empty();
        }

        RestOilPriceEntity oilPrice = upsertOilPrice(serviceAreaCode2.get(), fetchedItem.get());
        return Optional.of(OilInfoResponse.from(Optional.of(oilPrice), conveniences));
    }

    private List<RestOilEntity> findOilStationConveniences(RestStopEntity restStop) {
        String normalizedStationName = RestOilEntity.normalizeStationName(restStop.getUnitName());
        return restOilRepository.findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc(
                restStop.getRouteNo(), normalizedStationName);
    }

    private Optional<String> firstServiceAreaCode2(List<RestOilEntity> conveniences) {
        return conveniences.stream()
                .map(RestOilEntity::getStandardRestCode)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    private Optional<RestOilPriceItem> fetchOilPrice(String serviceAreaCode2) {
        RestOilPriceResponse response = exApiClient.getCurStateStationByServiceAreaCode2(serviceAreaCode2);
        if (response.getList() == null) {
            return Optional.empty();
        }

        return response.getList().stream().findFirst();
    }

    private RestOilPriceEntity upsertOilPrice(String serviceAreaCode2, RestOilPriceItem item) {
        return transactionTemplate.execute(status -> restOilPriceRepository
                .findByServiceAreaCode2(serviceAreaCode2)
                .map(entity -> updateOilPrice(entity, item))
                .orElseGet(() -> restOilPriceRepository.save(RestOilPriceEntity.from(item))));
    }

    private RestOilPriceEntity updateOilPrice(RestOilPriceEntity entity, RestOilPriceItem item) {
        entity.updateFrom(item);
        return entity;
    }
}
