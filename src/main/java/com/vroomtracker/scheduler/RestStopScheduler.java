package com.vroomtracker.scheduler;

import com.vroomtracker.service.HighwayServiceAreaInfoSyncService;
import com.vroomtracker.service.RestStopDetailSyncService;
import com.vroomtracker.service.RestStopSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestStopScheduler {

    private final RestStopSyncService restStopSyncService;
    private final RestStopDetailSyncService restStopDetailSyncService;
    private final HighwayServiceAreaInfoSyncService highwayServiceAreaInfoSyncService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void syncRestStopsDaily() {
        int savedCount = restStopSyncService.refreshRestStops();
        int detailSavedCount = restStopDetailSyncService.refreshRestStopDetails();
        int serviceAreaInfoSavedCount = highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos();
        log.info(
                "Rest stop sync completed. savedCount={}, detailSavedCount={}, serviceAreaInfoSavedCount={}",
                savedCount,
                detailSavedCount,
                serviceAreaInfoSavedCount);
    }
}
