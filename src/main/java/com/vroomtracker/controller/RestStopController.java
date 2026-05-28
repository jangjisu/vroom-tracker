package com.vroomtracker.controller;

import com.vroomtracker.common.ApiResponse;
import com.vroomtracker.controller.response.RestStopItemResponse;
import com.vroomtracker.service.RestStopQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rest-stops")
public class RestStopController {

    private final RestStopQueryService restStopQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestStopItemResponse>>> getRestStops() {
        List<RestStopItemResponse> restStops = restStopQueryService.findAll().stream()
                .map(RestStopItemResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(restStops));
    }
}
