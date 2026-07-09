package com.restroute.service;

import com.restroute.domain.HighwayServiceAreaInfoEntity;
import com.restroute.domain.RestFoodEntity;
import com.restroute.domain.RestOilEntity;
import com.restroute.domain.RestOilPriceEntity;
import com.restroute.domain.RestStopDetailEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.HighwayServiceAreaInfoRepository;
import com.restroute.repository.RestFoodRepository;
import com.restroute.repository.RestOilPriceRepository;
import com.restroute.repository.RestOilRepository;
import com.restroute.repository.RestStopDetailRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RestStopRelatedInfoQueryService {

    private final RestStopDetailRepository restStopDetailRepository;
    private final HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;
    private final RestOilRepository restOilRepository;
    private final RestOilPriceRepository restOilPriceRepository;
    private final RestFoodRepository restFoodRepository;

    @Transactional(readOnly = true)
    public RestStopRelatedInfo findByRestStop(RestStopEntity restStop) {
        String serviceAreaCode = restStop.getServiceAreaCode();
        Optional<RestStopDetailEntity> detail = findDetail(restStop);
        List<HighwayServiceAreaInfoEntity> infos = findHighwayServiceAreaInfos(serviceAreaCode);
        List<RestOilEntity> oilConveniences = findOilStationConveniences(restStop);
        Optional<String> oilServiceAreaCode2 = firstOilServiceAreaCode2(oilConveniences);
        Optional<RestOilPriceEntity> oilPrice = findOilPrice(serviceAreaCode, oilServiceAreaCode2);
        List<RestFoodEntity> foods = findFoods(restStop);

        return RestStopRelatedInfo.of(detail, infos, oilConveniences, oilServiceAreaCode2, oilPrice, foods);
    }

    private Optional<RestStopDetailEntity> findDetail(RestStopEntity restStop) {
        Optional<RestStopDetailEntity> detail =
                restStopDetailRepository.findByRestStopServiceAreaCode(restStop.getServiceAreaCode());
        if (detail.isPresent()) {
            return detail;
        }

        return restStopDetailRepository.findByServiceAreaCode(restStop.getServiceAreaCode());
    }

    private List<HighwayServiceAreaInfoEntity> findHighwayServiceAreaInfos(String serviceAreaCode) {
        List<HighwayServiceAreaInfoEntity> infos =
                highwayServiceAreaInfoRepository.findAllByRestStopServiceAreaCode(serviceAreaCode);
        if (!infos.isEmpty()) {
            return infos;
        }

        return highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode(serviceAreaCode);
    }

    private List<RestOilEntity> findOilStationConveniences(RestStopEntity restStop) {
        List<RestOilEntity> oilConveniences =
                restOilRepository.findAllByRestStopServiceAreaCodeOrderByIdAsc(restStop.getServiceAreaCode());
        if (!oilConveniences.isEmpty()) {
            return oilConveniences;
        }

        String normalizedStationName = RestOilEntity.normalizeStationName(restStop.getUnitName());
        return restOilRepository.findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc(
                restStop.getRouteNo(), normalizedStationName);
    }

    private Optional<RestOilPriceEntity> findOilPrice(String serviceAreaCode, Optional<String> oilServiceAreaCode2) {
        if (oilServiceAreaCode2.isEmpty()) {
            return Optional.empty();
        }

        Optional<RestOilPriceEntity> oilPrice =
                restOilPriceRepository.findAllByRestStopServiceAreaCodeOrderByIdAsc(serviceAreaCode).stream()
                        .findFirst();
        if (oilPrice.isPresent()) {
            return oilPrice;
        }

        return oilServiceAreaCode2.flatMap(restOilPriceRepository::findByServiceAreaCode2);
    }

    private List<RestFoodEntity> findFoods(RestStopEntity restStop) {
        List<RestFoodEntity> foods =
                restFoodRepository.findAllByRestStopServiceAreaCodeOrderByIdAsc(restStop.getServiceAreaCode());
        if (!foods.isEmpty()) {
            return foods;
        }

        return restFoodRepository.findAllByStdRestCdOrderByIdAsc(restStop.getStdRestCd());
    }

    private Optional<String> firstOilServiceAreaCode2(List<RestOilEntity> oilConveniences) {
        return oilConveniences.stream()
                .map(RestOilEntity::getStandardRestCode)
                .filter(StringUtils::hasText)
                .findFirst();
    }
}
