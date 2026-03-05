package com.vroomtracker.scheduler;

import com.vroomtracker.service.TrafficFlowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrafficFlowSchedulerTest {

    @Mock
    private TrafficFlowService trafficFlowService;

    @InjectMocks
    private TrafficFlowScheduler trafficFlowScheduler;

    @Test
    @DisplayName("refreshCurrentYearData_callsRefreshWithCurrentYear")
    void refreshCurrentYearData_callsRefreshWithCurrentYear() {
        String expectedYear = String.valueOf(LocalDateTime.now().getYear());

        trafficFlowScheduler.refreshCurrentYearData();

        verify(trafficFlowService).refreshByYear(eq(expectedYear));
    }
}
