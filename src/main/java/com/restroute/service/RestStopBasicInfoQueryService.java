package com.restroute.service;

import com.restroute.controller.response.RestStopBasicInfoResponse;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RestStopRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestStopBasicInfoQueryService {

    private final RestStopRepository restStopRepository;
    private final RestStopRelatedInfoQueryService restStopRelatedInfoQueryService;

    @Transactional(readOnly = true)
    public Optional<RestStopBasicInfoResponse> findByServiceAreaCode(String serviceAreaCode) {
        return restStopRepository.findByServiceAreaCode(serviceAreaCode).map(this::findByRestStop);
    }

    private RestStopBasicInfoResponse findByRestStop(RestStopEntity restStop) {
        RestStopRelatedInfo relatedInfo = restStopRelatedInfoQueryService.findByRestStop(restStop);
        return RestStopBasicInfoResponse.of(restStop, relatedInfo.detail());
    }
}
