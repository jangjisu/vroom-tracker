package com.restroute.scheduler;

import com.restroute.service.HighwayServiceAreaInfoSyncService;
import com.restroute.service.RestFoodSyncService;
import com.restroute.service.RestOilPriceSyncService;
import com.restroute.service.RestOilSyncService;
import com.restroute.service.RestStopDetailSyncService;
import com.restroute.service.RestStopSyncService;
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
    private final RestOilSyncService restOilSyncService;
    private final RestOilPriceSyncService restOilPriceSyncService;
    private final RestFoodSyncService restFoodSyncService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void syncRestStopsDaily() {
        refreshRestStops();
        refreshRestStopDetails();
        refreshHighwayServiceAreaInfos();
        refreshRestOils();
        refreshRestFoods();
    }

    @Scheduled(cron = "0 0 */3 * * *", zone = "Asia/Seoul")
    public void syncRestOilPricesEveryThreeHours() {
        refreshRestOilPrices();
    }

    private void refreshRestStops() {
        try {
            int savedCount = restStopSyncService.refreshRestStops();
            log.info("Scheduled rest stop sync completed. savedCount={}", savedCount);
        } catch (RuntimeException e) {
            log.error("Scheduled rest stop sync failed. cause={}", e.getMessage(), e);
        }
    }

    private void refreshRestStopDetails() {
        try {
            int savedCount = restStopDetailSyncService.refreshRestStopDetails();
            log.info("Scheduled rest stop detail sync completed. savedCount={}", savedCount);
        } catch (RuntimeException e) {
            log.error("Scheduled rest stop detail sync failed. cause={}", e.getMessage(), e);
        }
    }

    private void refreshHighwayServiceAreaInfos() {
        try {
            int savedCount = highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos();
            log.info("Scheduled highway service area info sync completed. savedCount={}", savedCount);
        } catch (RuntimeException e) {
            log.error("Scheduled highway service area info sync failed. cause={}", e.getMessage(), e);
        }
    }

    private void refreshRestOils() {
        try {
            int savedCount = restOilSyncService.refreshRestOils();
            log.info("Scheduled rest oil sync completed. savedCount={}", savedCount);
        } catch (RuntimeException e) {
            log.error("Scheduled rest oil sync failed. cause={}", e.getMessage(), e);
        }
    }

    private void refreshRestOilPrices() {
        try {
            int savedCount = restOilPriceSyncService.refreshRestOilPrices();
            log.info("Scheduled rest oil price sync completed. savedCount={}", savedCount);
        } catch (RuntimeException e) {
            log.error("Scheduled rest oil price sync failed. cause={}", e.getMessage(), e);
        }
    }

    private void refreshRestFoods() {
        try {
            int savedCount = restFoodSyncService.refreshRestFoods();
            log.info("Scheduled rest food sync completed. savedCount={}", savedCount);
        } catch (RuntimeException e) {
            log.error("Scheduled rest food sync failed. cause={}", e.getMessage(), e);
        }
    }
}
