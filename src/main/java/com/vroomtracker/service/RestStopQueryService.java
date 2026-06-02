package com.vroomtracker.service;

import com.vroomtracker.controller.response.RestStopDetailViewResponse;
import com.vroomtracker.domain.HighwayServiceAreaInfoEntity;
import com.vroomtracker.domain.RestStopDetailEntity;
import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.HighwayServiceAreaInfoRepository;
import com.vroomtracker.repository.RestStopDetailRepository;
import com.vroomtracker.repository.RestStopRepository;
import java.util.List;
import java.util.Optional;
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
        Optional<RestStopDetailEntity> detail = restStopDetailRepository.findByServiceAreaCode(serviceAreaCode);
        List<HighwayServiceAreaInfoEntity> infos =
                highwayServiceAreaInfoRepository.findAllByBusinessFacilityCode(serviceAreaCode);

        return RestStopDetailViewResponse.of(restStop, detail, infos);
    }
}
