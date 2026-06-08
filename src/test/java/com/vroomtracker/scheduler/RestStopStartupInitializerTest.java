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
import org.springframework.boot.ApplicationArguments;

@ExtendWith(MockitoExtension.class)
class RestStopStartupInitializerTest {

    @Mock
    private RestStopSyncService restStopSyncService;

    @Mock
    private RestStopDetailSyncService restStopDetailSyncService;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private RestStopStartupInitializer restStopStartupInitializer;

    @Test
    @DisplayName("서버 시작 시 휴게소 위치와 상세 초기 적재를 service에 위임한다")
    void run_delegatesInitialSyncToService() {
        when(restStopSyncService.initializeRestStopsIfEmpty()).thenReturn(203);
        when(restStopDetailSyncService.initializeRestStopDetailsIfEmpty()).thenReturn(215);

        restStopStartupInitializer.run(applicationArguments);

        verify(restStopSyncService).initializeRestStopsIfEmpty();
        verify(restStopDetailSyncService).initializeRestStopDetailsIfEmpty();
    }
}
