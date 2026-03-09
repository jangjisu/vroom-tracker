package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.domain.TrafficFlowEntity;
import com.vroomtracker.dto.TrafficFlowDto;
import com.vroomtracker.repository.TrafficFlowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrafficFlowServiceTest {

    @Mock
    private TrafficFlowRepository trafficFlowRepository;

    @Mock
    private ExApiClient exApiClient;

    @InjectMocks
    private TrafficFlowService trafficFlowService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(trafficFlowService, "apiKey", "test-key");
    }

    // ================================================================
    // findByYear
    // ================================================================

    @Nested
    @DisplayName("findByYear")
    class FindByYear {

        @Test
        @DisplayName("findByYear_whenDataExists_returnsMappedDtos")
        void findByYear_whenDataExists_returnsMappedDtos() {
            when(trafficFlowRepository.findByStdYear("2024"))
                    .thenReturn(List.of(
                            flowEntity("2024", "평일", "당일", "14", "1000"),
                            flowEntity("2024", "토요일", "당일", "15", "1500")
                    ));

            List<TrafficFlowDto> result = trafficFlowService.findByYear("2024");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDayType()).isEqualTo("평일");
            assertThat(result.get(1).getHour()).isEqualTo("15");
        }

        @Test
        @DisplayName("findByYear_whenNoData_returnsEmptyList")
        void findByYear_whenNoData_returnsEmptyList() {
            when(trafficFlowRepository.findByStdYear(anyString()))
                    .thenReturn(Collections.emptyList());

            assertThat(trafficFlowService.findByYear("2024")).isEmpty();
        }
    }

    // ================================================================
    // refreshByYear
    // ================================================================

    @Nested
    @DisplayName("refreshByYear")
    class RefreshByYear {

        @Test
        @DisplayName("refreshByYear_whenApiSuccess_deletesOldAndSavesNew")
        void refreshByYear_whenApiSuccess_deletesOldAndSavesNew() {
            stubFlowApi("2024", List.of(
                    flowItem("2024", "평일", "0", "당일", "0", "14", "1000"),
                    flowItem("2024", "평일", "0", "당일", "0", "15", "1200")
            ));

            trafficFlowService.refreshByYear("2024");

            verify(trafficFlowRepository).deleteByStdYear("2024");
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<TrafficFlowEntity>> captor = ArgumentCaptor.forClass(List.class);
            verify(trafficFlowRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(2);
        }

        @Test
        @DisplayName("refreshByYear_whenApiReturnsEmpty_keepsExistingData")
        void refreshByYear_whenApiReturnsEmpty_keepsExistingData() {
            stubFlowApi("2024", Collections.emptyList());

            trafficFlowService.refreshByYear("2024");

            verify(trafficFlowRepository, never()).deleteByStdYear(anyString());
            verify(trafficFlowRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("refreshByYear_whenApiFails_keepsExistingData")
        void refreshByYear_whenApiFails_keepsExistingData() {
            when(exApiClient.getTrafficFlowByTime(any(), any(), any()))
                    .thenThrow(new RuntimeException("connection refused"));

            trafficFlowService.refreshByYear("2024");

            verify(trafficFlowRepository, never()).deleteByStdYear(anyString());
        }

        @Test
        @DisplayName("refreshByYear_whenApiFailureCode_keepsExistingData")
        void refreshByYear_whenApiFailureCode_keepsExistingData() {
            ExApiClient.TrafficFlowResponse response = new ExApiClient.TrafficFlowResponse();
            response.setCode("99");
            when(exApiClient.getTrafficFlowByTime(any(), any(), any())).thenReturn(response);

            trafficFlowService.refreshByYear("2024");

            verify(trafficFlowRepository, never()).deleteByStdYear(anyString());
        }

        @Test
        @DisplayName("refreshByYear_savedEntitiesHaveFetchedAt")
        void refreshByYear_savedEntitiesHaveFetchedAt() {
            stubFlowApi("2024", List.of(
                    flowItem("2024", "평일", "0", "당일", "0", "14", "1000")
            ));

            trafficFlowService.refreshByYear("2024");

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<TrafficFlowEntity>> captor = ArgumentCaptor.forClass(List.class);
            verify(trafficFlowRepository).saveAll(captor.capture());
            assertThat(captor.getValue().get(0).getFetchedAt()).isNotNull();
        }
    }

    // ================================================================
    // 헬퍼
    // ================================================================

    private TrafficFlowEntity flowEntity(String year, String dfttNm, String scopTypeNm,
                                          String stdHour, String trfl) {
        return TrafficFlowEntity.builder()
                .stdYear(year)
                .sphlDfttNm(dfttNm)
                .sphlDfttCode("0")
                .sphlDfttScopTypeNm(scopTypeNm)
                .sphlDfttScopTypeCode("0")
                .stdHour(stdHour)
                .trfl(trfl)
                .fetchedAt(LocalDateTime.now())
                .build();
    }

    private ExApiClient.TrafficFlowItem flowItem(String year, String dfttNm, String dfttCode,
                                                   String scopTypeNm, String scopTypeCode,
                                                   String stdHour, String trfl) {
        ExApiClient.TrafficFlowItem item = new ExApiClient.TrafficFlowItem();
        item.setStdYear(year);
        item.setSphlDfttNm(dfttNm);
        item.setSphlDfttCode(dfttCode);
        item.setSphlDfttScopTypeNm(scopTypeNm);
        item.setSphlDfttScopTypeCode(scopTypeCode);
        item.setStdHour(stdHour);
        item.setTrfl(trfl);
        return item;
    }

    private void stubFlowApi(String year, List<ExApiClient.TrafficFlowItem> items) {
        ExApiClient.TrafficFlowResponse response = new ExApiClient.TrafficFlowResponse();
        response.setCode("00");
        response.setList(items);
        when(exApiClient.getTrafficFlowByTime(any(), any(), eq(year))).thenReturn(response);
    }
}
