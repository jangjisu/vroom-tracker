package com.restroute.service;

import com.restroute.client.ExApiClient;
import com.restroute.client.response.RestOilPriceItem;
import com.restroute.client.response.RestOilPriceResponse;
import com.restroute.controller.response.OilInfoResponse;
import com.restroute.domain.RestOilEntity;
import com.restroute.domain.RestOilPriceEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RestOilPriceRepository;
import com.restroute.repository.RestOilRepository;
import com.restroute.repository.RestStopRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RestOilPriceRefreshService {

    private static final Duration REFRESH_TTL = Duration.ofMinutes(10);

    private final RestStopRepository restStopRepository;
    private final RestOilRepository restOilRepository;
    private final RestOilPriceRepository restOilPriceRepository;
    private final ExApiClient exApiClient;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public Optional<OilInfoResponse> refreshByServiceAreaCode(String serviceAreaCode) {
        return restStopRepository.findByServiceAreaCode(serviceAreaCode).flatMap(this::refreshByRestStop);
    }

    private Optional<OilInfoResponse> refreshByRestStop(RestStopEntity restStop) {
        List<RestOilEntity> conveniences = findOilStationConveniences(restStop);
        Optional<String> serviceAreaCode2 = firstServiceAreaCode2(conveniences);
        if (serviceAreaCode2.isEmpty()) {
            return Optional.empty();
        }

        Optional<RestOilPriceEntity> cachedOilPrice =
                restOilPriceRepository.findByServiceAreaCode2(serviceAreaCode2.get());
        if (cachedOilPrice.filter(this::isFresh).isPresent()) {
            return Optional.of(OilInfoResponse.from(cachedOilPrice, conveniences));
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
        LocalDateTime refreshedAt = LocalDateTime.now(clock);
        return transactionTemplate.execute(status -> restOilPriceRepository
                .findByServiceAreaCode2(serviceAreaCode2)
                .map(entity -> updateOilPrice(entity, item, refreshedAt))
                .orElseGet(() -> restOilPriceRepository.save(RestOilPriceEntity.from(item, refreshedAt))));
    }

    private RestOilPriceEntity updateOilPrice(
            RestOilPriceEntity entity, RestOilPriceItem item, LocalDateTime refreshedAt) {
        entity.updateFrom(item, refreshedAt);
        return entity;
    }

    private boolean isFresh(RestOilPriceEntity oilPrice) {
        if (oilPrice.getLastRefreshedAt() == null) {
            return false;
        }

        return !oilPrice.getLastRefreshedAt().isBefore(LocalDateTime.now(clock).minus(REFRESH_TTL));
    }
}
