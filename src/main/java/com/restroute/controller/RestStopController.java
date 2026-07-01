package com.restroute.controller;

import com.restroute.common.ApiResponse;
import com.restroute.common.ResponseCode;
import com.restroute.controller.response.OilInfoResponse;
import com.restroute.controller.response.RestStopDetailViewResponse;
import com.restroute.controller.response.RestStopItemResponse;
import com.restroute.service.RestOilPriceRefreshService;
import com.restroute.service.RestStopQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rest-stops")
public class RestStopController {

    private final RestStopQueryService restStopQueryService;
    private final RestOilPriceRefreshService restOilPriceRefreshService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestStopItemResponse>>> getRestStops() {
        List<RestStopItemResponse> restStops = restStopQueryService.findAll().stream()
                .map(RestStopItemResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(restStops));
    }

    @GetMapping("/{serviceAreaCode}")
    public ResponseEntity<ApiResponse<RestStopDetailViewResponse>> getRestStopDetail(
            @PathVariable String serviceAreaCode) {
        return restStopQueryService
                .findDetailByServiceAreaCode(serviceAreaCode)
                .map(restStop -> ResponseEntity.ok(ApiResponse.success(restStop)))
                .orElseGet(() -> ResponseEntity.status(ResponseCode.NOT_FOUND.getHttpStatus())
                        .body(ApiResponse.error(ResponseCode.NOT_FOUND)));
    }

    @PostMapping("/{serviceAreaCode}/oil-price/refresh")
    public ResponseEntity<ApiResponse<OilInfoResponse>> refreshRestOilPrice(@PathVariable String serviceAreaCode) {
        return restOilPriceRefreshService
                .refreshByServiceAreaCode(serviceAreaCode)
                .map(oilInfo -> ResponseEntity.ok(ApiResponse.success(oilInfo)))
                .orElseGet(() -> ResponseEntity.status(ResponseCode.NOT_FOUND.getHttpStatus())
                        .body(ApiResponse.error(ResponseCode.NOT_FOUND)));
    }
}
