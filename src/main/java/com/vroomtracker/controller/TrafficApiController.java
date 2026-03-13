package com.vroomtracker.controller;

import com.vroomtracker.common.ApiResponse;
import com.vroomtracker.dto.NationwideTrafficDto;
import com.vroomtracker.dto.RegionTrafficDto;
import com.vroomtracker.dto.TollGateTrafficDto;
import com.vroomtracker.dto.TrafficFlowDto;
import com.vroomtracker.service.TrafficFlowService;
import com.vroomtracker.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrafficApiController {

    private final TrafficService trafficService;
    private final TrafficFlowService trafficFlowService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<NationwideTrafficDto>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(trafficService.getDashboardData(20).summary()));
    }

    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<List<TollGateTrafficDto>>> getRanking() {
        return ResponseEntity.ok(ApiResponse.success(trafficService.getDashboardData(20).ranking()));
    }

    @GetMapping("/hourly-pattern")
    public ResponseEntity<ApiResponse<List<TrafficFlowDto>>> getHourlyPattern() {
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        return ResponseEntity.ok(ApiResponse.success(trafficFlowService.findByYear(currentYear)));
    }

    @GetMapping("/region-ranking")
    public ResponseEntity<ApiResponse<List<RegionTrafficDto>>> getRegionRanking() {
        return ResponseEntity.ok(ApiResponse.success(trafficService.getRegionRanking()));
    }

    @PostMapping("/hourly-pattern/init")
    public ResponseEntity<ApiResponse<Void>> initHourlyPattern() {
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        trafficFlowService.initIfEmpty(currentYear);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
