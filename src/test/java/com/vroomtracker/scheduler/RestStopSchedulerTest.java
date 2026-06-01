package com.vroomtracker.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vroomtracker.service.RestStopDetailSyncService;
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

    @Mock
    private RestStopDetailSyncService restStopDetailSyncService;

    @InjectMocks
    private RestStopScheduler restStopScheduler;

    @Test
    @DisplayName("매일 휴게소 위치와 상세 정보 동기화를 service에 위임한다")
    void syncRestStopsDaily_delegatesToService() {
        when(restStopSyncService.refreshRestStops()).thenReturn(203);
        when(restStopDetailSyncService.refreshRestStopDetails()).thenReturn(215);

        restStopScheduler.syncRestStopsDaily();

        verify(restStopSyncService).refreshRestStops();
        verify(restStopDetailSyncService).refreshRestStopDetails();
    }
}
