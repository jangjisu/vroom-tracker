package com.restroute.service;

import com.restroute.controller.response.RestStopDetailViewResponse;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RestStopRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestStopQueryService {

    private final RestStopRepository restStopRepository;
    private final RestStopRelatedInfoQueryService restStopRelatedInfoQueryService;

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
        RestStopRelatedInfo relatedInfo = restStopRelatedInfoQueryService.findByRestStop(restStop);
        return RestStopDetailViewResponse.of(
                restStop,
                relatedInfo.detail(),
                relatedInfo.highwayServiceAreaInfos(),
                relatedInfo.oilStationConveniences(),
                relatedInfo.oilPrice(),
                relatedInfo.foods());
    }
}
