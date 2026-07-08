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
        Optional<RestStopDetailEntity> detail =
                restStopDetailRepository.findByServiceAreaCode(restStop.getServiceAreaCode());
        List<HighwayServiceAreaInfoEntity> infos =
                highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode(restStop.getServiceAreaCode());
        List<RestOilEntity> oilConveniences = findOilStationConveniences(restStop);
        Optional<String> oilServiceAreaCode2 = firstOilServiceAreaCode2(oilConveniences);
        Optional<RestOilPriceEntity> oilPrice =
                oilServiceAreaCode2.flatMap(restOilPriceRepository::findByServiceAreaCode2);
        List<RestFoodEntity> foods = restFoodRepository.findAllByStdRestCdOrderByIdAsc(restStop.getStdRestCd());

        return RestStopRelatedInfo.of(detail, infos, oilConveniences, oilServiceAreaCode2, oilPrice, foods);
    }

    private List<RestOilEntity> findOilStationConveniences(RestStopEntity restStop) {
        String normalizedStationName = RestOilEntity.normalizeStationName(restStop.getUnitName());
        return restOilRepository.findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc(
                restStop.getRouteNo(), normalizedStationName);
    }

    private Optional<String> firstOilServiceAreaCode2(List<RestOilEntity> oilConveniences) {
        return oilConveniences.stream()
                .map(RestOilEntity::getStandardRestCode)
                .filter(StringUtils::hasText)
                .findFirst();
    }
}
