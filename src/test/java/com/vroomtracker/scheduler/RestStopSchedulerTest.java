package com.vroomtracker.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vroomtracker.service.RestStopSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestStopSchedulerTest {

    @Mock
    private RestStopSyncService restStopSyncService;

    @InjectMocks
    private RestStopScheduler restStopScheduler;

    @Test
    @DisplayName("매일 휴게소 동기화를 service에 위임한다")
    void syncRestStopsDaily_delegatesToService() {
        when(restStopSyncService.refreshRestStops()).thenReturn(203);

        restStopScheduler.syncRestStopsDaily();

        verify(restStopSyncService).refreshRestStops();
    }
}
