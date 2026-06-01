package com.vroomtracker.controller;

import com.vroomtracker.common.ApiResponse;
import com.vroomtracker.controller.response.MapConfigResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map-config")
public class MapConfigController {

    private final String naverMapsNcpKeyId;

    public MapConfigController(@Value("${naver.maps.ncp-key-id:}") String naverMapsNcpKeyId) {
        this.naverMapsNcpKeyId = naverMapsNcpKeyId;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MapConfigResponse>> getMapConfig() {
        return ResponseEntity.ok(ApiResponse.success(MapConfigResponse.of(naverMapsNcpKeyId)));
    }
}
