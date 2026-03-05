package com.vroomtracker.scheduler;

import com.vroomtracker.service.TrafficFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficFlowScheduler {

    private final TrafficFlowService trafficFlowService;

    /**
     * 매일 새벽 1시에 현재 연도의 시간대별 교통량 통계를 갱신합니다.
     * trafficFlowByTime API 는 연간 집계 데이터이므로 일 1회 갱신으로 충분합니다.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void refreshCurrentYearData() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        log.info("trafficFlow 정기 갱신 시작: {}년", year);
        trafficFlowService.refreshByYear(year);
        log.info("trafficFlow 정기 갱신 완료: {}년", year);
    }
}
