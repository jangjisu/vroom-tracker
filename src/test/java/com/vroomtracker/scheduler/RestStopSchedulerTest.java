package com.vroomtracker.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vroomtracker.service.HighwayServiceAreaInfoSyncService;
import com.vroomtracker.service.RestOilSyncService;
import com.vroomtracker.service.RestStopDetailSyncService;
import com.vroomtracker.service.RestStopSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class RestStopSchedulerTest {

    @Mock
    private RestStopSyncService restStopSyncService;

    @Mock
    private RestStopDetailSyncService restStopDetailSyncService;

    @Mock
    private HighwayServiceAreaInfoSyncService highwayServiceAreaInfoSyncService;

    @Mock
    private RestOilSyncService restOilSyncService;

    @InjectMocks
    private RestStopScheduler restStopScheduler;

    @Test
    @DisplayName("매일 휴게소 위치와 상세, 고속도로 휴게소 정보, 주유소 편의시설 동기화를 service에 위임한다")
    void syncRestStopsDaily_delegatesToService() {
        when(restStopSyncService.refreshRestStops()).thenReturn(203);
        when(restStopDetailSyncService.refreshRestStopDetails()).thenReturn(215);
        when(highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos()).thenReturn(581);
        when(restOilSyncService.refreshRestOils()).thenReturn(429);

        restStopScheduler.syncRestStopsDaily();

        verify(restStopSyncService).refreshRestStops();
        verify(restStopDetailSyncService).refreshRestStopDetails();
        verify(highwayServiceAreaInfoSyncService).refreshHighwayServiceAreaInfos();
        verify(restOilSyncService).refreshRestOils();
    }

    @Test
    @DisplayName("휴게소 상세 동기화가 실패해도 고속도로 휴게소 정보 동기화를 계속한다")
    void syncRestStopsDaily_continuesServiceAreaInfoSyncWhenDetailSyncFails(CapturedOutput output) {
        when(restStopSyncService.refreshRestStops()).thenReturn(203);
        when(restStopDetailSyncService.refreshRestStopDetails())
                .thenThrow(new IllegalStateException("detail page 2 failed"));
        when(highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos()).thenReturn(581);

        assertThatCode(restStopScheduler::syncRestStopsDaily).doesNotThrowAnyException();

        verify(highwayServiceAreaInfoSyncService).refreshHighwayServiceAreaInfos();
        verify(restOilSyncService).refreshRestOils();
        assertThat(output)
                .contains("Scheduled rest stop detail sync failed.")
                .contains("detail page 2 failed")
                .contains("Scheduled highway service area info sync completed. savedCount=581");
    }

    @Test
    @DisplayName("휴게소 위치 동기화가 실패해도 나머지 동기화를 계속한다")
    void syncRestStopsDaily_continuesRemainingSyncsWhenRestStopSyncFails(CapturedOutput output) {
        when(restStopSyncService.refreshRestStops()).thenThrow(new IllegalStateException("location API failed"));
        when(restStopDetailSyncService.refreshRestStopDetails()).thenReturn(215);
        when(highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos()).thenReturn(581);

        assertThatCode(restStopScheduler::syncRestStopsDaily).doesNotThrowAnyException();

        verify(restStopDetailSyncService).refreshRestStopDetails();
        verify(highwayServiceAreaInfoSyncService).refreshHighwayServiceAreaInfos();
        verify(restOilSyncService).refreshRestOils();
        assertThat(output)
                .contains("Scheduled rest stop sync failed.")
                .contains("location API failed")
                .contains("Scheduled rest stop detail sync completed. savedCount=215");
    }

    @Test
    @DisplayName("고속도로 휴게소 정보 동기화 실패를 로그로 기록하고 전파하지 않는다")
    void syncRestStopsDaily_doesNotPropagateServiceAreaInfoFailure(CapturedOutput output) {
        when(restStopSyncService.refreshRestStops()).thenReturn(203);
        when(restStopDetailSyncService.refreshRestStopDetails()).thenReturn(215);
        when(highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos())
                .thenThrow(new IllegalStateException("service area info API failed"));

        assertThatCode(restStopScheduler::syncRestStopsDaily).doesNotThrowAnyException();

        assertThat(output)
                .contains("Scheduled highway service area info sync failed.")
                .contains("service area info API failed");
        verify(restOilSyncService).refreshRestOils();
    }

    @Test
    @DisplayName("주유소 편의시설 동기화 실패를 로그로 기록하고 전파하지 않는다")
    void syncRestStopsDaily_doesNotPropagateRestOilFailure(CapturedOutput output) {
        when(restStopSyncService.refreshRestStops()).thenReturn(203);
        when(restStopDetailSyncService.refreshRestStopDetails()).thenReturn(215);
        when(highwayServiceAreaInfoSyncService.refreshHighwayServiceAreaInfos()).thenReturn(581);
        when(restOilSyncService.refreshRestOils()).thenThrow(new IllegalStateException("rest oil API failed"));

        assertThatCode(restStopScheduler::syncRestStopsDaily).doesNotThrowAnyException();

        assertThat(output).contains("Scheduled rest oil sync failed.").contains("rest oil API failed");
    }
}
