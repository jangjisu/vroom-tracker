package com.restroute.service;

import com.restroute.controller.response.RestStopDetailViewResponse;
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
import com.restroute.repository.RestStopRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RestStopQueryService {

    private final RestStopRepository restStopRepository;
    private final RestStopDetailRepository restStopDetailRepository;
    private final HighwayServiceAreaInfoRepository highwayServiceAreaInfoRepository;
    private final RestOilRepository restOilRepository;
    private final RestOilPriceRepository restOilPriceRepository;
    private final RestFoodRepository restFoodRepository;

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
        Optional<RestStopDetailEntity> detail = restStopDetailRepository.findByServiceAreaCode(serviceAreaCode);
        List<HighwayServiceAreaInfoEntity> infos =
                highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode(serviceAreaCode);
        String normalizedStationName = RestOilEntity.normalizeStationName(restStop.getUnitName());
        List<RestOilEntity> oilStationConveniences =
                restOilRepository.findAllByRouteCodeAndNormalizedStationNameOrderByIdAsc(
                        restStop.getRouteNo(), normalizedStationName);
        Optional<RestOilPriceEntity> oilPrice = findOilPrice(oilStationConveniences);
        List<RestFoodEntity> foods = restFoodRepository.findAllByStdRestCdOrderByIdAsc(restStop.getStdRestCd());

        return RestStopDetailViewResponse.of(restStop, detail, infos, oilStationConveniences, oilPrice, foods);
    }

    private Optional<RestOilPriceEntity> findOilPrice(List<RestOilEntity> oilStationConveniences) {
        return oilStationConveniences.stream()
                .map(RestOilEntity::getStandardRestCode)
                .filter(StringUtils::hasText)
                .findFirst()
                .flatMap(restOilPriceRepository::findByServiceAreaCode2);
    }
}
