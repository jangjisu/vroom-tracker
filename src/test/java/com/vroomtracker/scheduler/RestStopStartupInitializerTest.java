package com.vroomtracker.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
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

    @Test
    @DisplayName("휴게소 위치 초기 동기화가 실패해도 상세 동기화를 계속한다")
    void run_continuesDetailSyncWhenRestStopSyncFails(CapturedOutput output) {
        when(restStopSyncService.initializeRestStopsIfEmpty())
                .thenThrow(new IllegalStateException("location API failed"));
        when(restStopDetailSyncService.initializeRestStopDetailsIfEmpty()).thenReturn(215);

        assertThatCode(() -> restStopStartupInitializer.run(applicationArguments))
                .doesNotThrowAnyException();

        verify(restStopDetailSyncService).initializeRestStopDetailsIfEmpty();
        assertThat(output)
                .contains("Initial rest stop sync failed.")
                .contains("location API failed")
                .contains("Initial rest stop detail sync completed. detailSavedCount=215");
    }

    @Test
    @DisplayName("휴게소 상세 초기 동기화 실패가 앱 시작으로 전파되지 않는다")
    void run_doesNotPropagateDetailSyncFailure(CapturedOutput output) {
        when(restStopSyncService.initializeRestStopsIfEmpty()).thenReturn(203);
        when(restStopDetailSyncService.initializeRestStopDetailsIfEmpty())
                .thenThrow(new IllegalStateException("detail API failed"));

        assertThatCode(() -> restStopStartupInitializer.run(applicationArguments))
                .doesNotThrowAnyException();

        assertThat(output).contains("Initial rest stop detail sync failed.").contains("detail API failed");
    }

    @Test
    @DisplayName("초기 적재할 데이터가 있으면 두 동기화를 생략했다고 기록한다")
    void run_logsSkippedSyncs(CapturedOutput output) {
        when(restStopSyncService.initializeRestStopsIfEmpty()).thenReturn(0);
        when(restStopDetailSyncService.initializeRestStopDetailsIfEmpty()).thenReturn(0);

        restStopStartupInitializer.run(applicationArguments);

        assertThat(output)
                .contains("Initial rest stop sync skipped because rest_stop table already has data.")
                .contains("Initial rest stop detail sync skipped because rest_stop_detail table already has data.");
    }
}
