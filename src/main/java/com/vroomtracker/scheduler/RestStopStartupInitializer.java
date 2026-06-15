package com.vroomtracker.scheduler;

import com.vroomtracker.service.RestStopDetailSyncService;
import com.vroomtracker.service.RestStopSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rest-stop.sync", name = "startup-enabled", havingValue = "true", matchIfMissing = true)
public class RestStopStartupInitializer implements ApplicationRunner {

    private final RestStopSyncService restStopSyncService;
    private final RestStopDetailSyncService restStopDetailSyncService;

    @Override
    public void run(ApplicationArguments args) {
        initializeRestStops();
        initializeRestStopDetails();
    }

    private void initializeRestStops() {
        try {
            int savedCount = restStopSyncService.initializeRestStopsIfEmpty();
            if (savedCount > 0) {
                log.info("Initial rest stop sync completed. savedCount={}", savedCount);
                return;
            }

            log.info("Initial rest stop sync skipped because rest_stop table already has data.");
        } catch (RuntimeException e) {
            log.error("Initial rest stop sync failed. cause={}", e.getMessage(), e);
        }
    }

    private void initializeRestStopDetails() {
        try {
            int detailSavedCount = restStopDetailSyncService.initializeRestStopDetailsIfEmpty();
            if (detailSavedCount > 0) {
                log.info("Initial rest stop detail sync completed. detailSavedCount={}", detailSavedCount);
                return;
            }

            log.info("Initial rest stop detail sync skipped because rest_stop_detail table already has data.");
        } catch (RuntimeException e) {
            log.error("Initial rest stop detail sync failed. cause={}", e.getMessage(), e);
        }
    }
}
