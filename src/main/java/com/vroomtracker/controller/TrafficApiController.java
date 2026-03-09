package com.vroomtracker.controller;

import com.vroomtracker.dto.NationwideTrafficDto;
import com.vroomtracker.dto.TollGateTrafficDto;
import com.vroomtracker.dto.TrafficFlowDto;
import com.vroomtracker.service.TrafficFlowService;
import com.vroomtracker.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

    /** 상단 요약 카드 (전국 출구 교통량 합계, 혼잡 영업소 수, 가장 붐비는 곳) */
    @GetMapping("/summary")
    public NationwideTrafficDto getSummary() {
        return trafficService.getDashboardData(20).summary();
    }

    /** 출구 교통량 TOP 20 랭킹 */
    @GetMapping("/ranking")
    public List<TollGateTrafficDto> getRanking() {
        return trafficService.getDashboardData(20).ranking();
    }

    /** 시간대별 교통량 패턴 (연간 통계) */
    @GetMapping("/hourly-pattern")
    public List<TrafficFlowDto> getHourlyPattern() {
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        return trafficFlowService.findByYear(currentYear);
    }
}
