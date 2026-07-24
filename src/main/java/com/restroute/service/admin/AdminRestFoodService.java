package com.restroute.service.admin;

import com.restroute.controller.request.AdminRestFoodRequest;
import com.restroute.controller.response.AdminRestFoodResponse;
import com.restroute.domain.RestFoodEntity;
import com.restroute.domain.RestStopEntity;
import com.restroute.repository.RestFoodRepository;
import com.restroute.repository.RestStopRepository;
import com.restroute.service.image.RestFoodNotFoundException;
import com.restroute.service.image.RestStopNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRestFoodService {

    private final RestFoodRepository restFoodRepository;
    private final RestStopRepository restStopRepository;

    @Transactional(readOnly = true)
    public List<AdminRestFoodResponse> findByServiceAreaCode(String serviceAreaCode) {
        return restFoodRepository.findAllByRestStopServiceAreaCodeOrderByIdAsc(serviceAreaCode).stream()
                .map(AdminRestFoodResponse::from)
                .toList();
    }

    @Transactional
    public AdminRestFoodResponse create(String serviceAreaCode, AdminRestFoodRequest request) {
        RestStopEntity restStop = restStopRepository
                .findByServiceAreaCode(serviceAreaCode)
                .orElseThrow(() -> RestStopNotFoundException.forServiceAreaCode(serviceAreaCode));

        RestFoodEntity created = RestFoodEntity.createByAdmin(
                serviceAreaCode,
                restStop.getStdRestCd(),
                request.foodName(),
                request.foodCost(),
                request.description());
        return AdminRestFoodResponse.from(restFoodRepository.save(created));
    }

    @Transactional
    public AdminRestFoodResponse update(String serviceAreaCode, Long foodId, AdminRestFoodRequest request) {
        RestFoodEntity entity = requireFood(serviceAreaCode, foodId);
        entity.applyAdminEdit(request.foodName(), request.foodCost(), request.description());
        return AdminRestFoodResponse.from(entity);
    }

    @Transactional
    public AdminRestFoodResponse clearOverride(String serviceAreaCode, Long foodId) {
        RestFoodEntity entity = requireFood(serviceAreaCode, foodId);
        entity.clearAdminOverride();
        return AdminRestFoodResponse.from(entity);
    }

    @Transactional
    public void delete(String serviceAreaCode, Long foodId) {
        RestFoodEntity entity = requireFood(serviceAreaCode, foodId);
        if (!entity.isAdminCreated()) {
            throw InvalidRestFoodEditException.forSyncedFoodDeletion(foodId);
        }
        restFoodRepository.delete(entity);
    }

    private RestFoodEntity requireFood(String serviceAreaCode, Long foodId) {
        return restFoodRepository
                .findByIdAndRestStopServiceAreaCode(foodId, serviceAreaCode)
                .orElseThrow(() -> RestFoodNotFoundException.forId(foodId));
    }
}
