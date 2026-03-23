package com.vroomtracker.service;

import com.vroomtracker.client.ExApiClient;
import com.vroomtracker.client.response.TrafficIcItem;
import com.vroomtracker.client.response.TrafficIcResponse;
import com.vroomtracker.client.response.TrafficRegionItem;
import com.vroomtracker.client.response.TrafficRegionResponse;
import com.vroomtracker.domain.CongestionLevel;
import com.vroomtracker.dto.NationwideTrafficDto;
import com.vroomtracker.dto.RegionTrafficDto;
import com.vroomtracker.dto.TollGateTrafficDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrafficServiceTest {

    @Mock
    private ExApiClient exApiClient;

    @InjectMocks
    private TrafficService trafficService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(trafficService, "apiKey", "test-api-key");
    }

    // ================================================================
    // getDashboardData - trafficIc 호출 처리
    // ================================================================

    @Nested
    @DisplayName("getDashboardData - trafficIc 호출 처리")
    class FetchExitTraffic {

        @Test
        @DisplayName("getTrafficIc_whenApiSuccess_returnsRanking")
        void getTrafficIc_whenApiSuccess_returnsRanking() {
            stubIcApi(List.of(
                    icItem("0010A", "서울", "1", "12.5", "도공", "2024030514"),
                    icItem("0020A", "부산", "1", "8.3", "도공", "2024030514")
            ));

            List<TollGateTrafficDto> ranking = trafficService.getDashboardData(10).ranking();

            assertThat(ranking).hasSize(2);
        }

        @Test
        @DisplayName("getTrafficIc_whenApiFailureCode_returnsEmptyRanking")
        void getTrafficIc_whenApiFailureCode_returnsEmptyRanking() {
            TrafficIcResponse response = new TrafficIcResponse();
            response.setCode("99");
            when(exApiClient.getTrafficIc(any(), any(), any(), any(), any(), any()))
                    .thenReturn(response);

            assertThat(trafficService.getDashboardData(10).ranking()).isEmpty();
        }

        @Test
        @DisplayName("getTrafficIc_whenFeignThrows_returnsEmptyRanking")
        void getTrafficIc_whenFeignThrows_returnsEmptyRanking() {
            when(exApiClient.getTrafficIc(any(), any(), any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("connection refused"));

            assertThat(trafficService.getDashboardData(10).ranking()).isEmpty();
        }

        @Test
        @DisplayName("getTrafficIc_whenListIsNull_returnsEmptyRanking")
        void getTrafficIc_whenListIsNull_returnsEmptyRanking() {
            TrafficIcResponse response = new TrafficIcResponse();
            response.setCode("SUCCESS");
            response.setList(null);
            when(exApiClient.getTrafficIc(any(), any(), any(), any(), any(), any()))
                    .thenReturn(response);

            assertThat(trafficService.getDashboardData(10).ranking()).isEmpty();
        }
    }

    // ================================================================
    // getDashboardData - ranking
    // ================================================================

    @Nested
    @DisplayName("getDashboardData - ranking")
    class Ranking {

        @Test
        @DisplayName("ranking_sortedByExitVolumeDescending")
        void ranking_sortedByExitVolumeDescending() {
            stubIcApi(List.of(
                    icItem("A", "대전", "1", "3.0", "도공", "14"),
                    icItem("B", "서울", "1", "12.5", "도공", "14"),
                    icItem("C", "부산", "1", "8.0", "도공", "14")
            ));

            List<TollGateTrafficDto> ranking = trafficService.getDashboardData(10).ranking();

            assertThat(ranking).extracting(TollGateTrafficDto::getUnitName)
                    .containsExactly("서울", "부산", "대전");
            assertThat(ranking).extracting(TollGateTrafficDto::getRank)
                    .containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("ranking_limitedByTopN")
        void ranking_limitedByTopN() {
            stubIcApi(List.of(
                    icItem("A", "IC1", "1", "10.0", "도공", "14"),
                    icItem("B", "IC2", "1", "9.0", "도공", "14"),
                    icItem("C", "IC3", "1", "8.0", "도공", "14"),
                    icItem("D", "IC4", "1", "7.0", "도공", "14"),
                    icItem("E", "IC5", "1", "6.0", "도공", "14")
            ));

            assertThat(trafficService.getDashboardData(3).ranking()).hasSize(3);
        }

        @Test
        @DisplayName("ranking_topRankHasBarWidth100")
        void ranking_topRankHasBarWidth100() {
            stubIcApi(List.of(
                    icItem("A", "서울", "1", "10.0", "도공", "14"),
                    icItem("B", "부산", "1", "5.0", "도공", "14")
            ));

            List<TollGateTrafficDto> ranking = trafficService.getDashboardData(5).ranking();

            assertThat(ranking.get(0).getBarWidth()).isEqualTo(100);
            assertThat(ranking.get(1).getBarWidth()).isEqualTo(50);
        }

        @Test
        @DisplayName("ranking_excludesEntranceItems")
        void ranking_excludesEntranceItems() {
            stubIcApi(List.of(
                    icItem("A", "입구영업소", "0", "99.0", "도공", "14"),
                    icItem("B", "출구영업소", "1", "5.0", "도공", "14")
            ));

            List<TollGateTrafficDto> ranking = trafficService.getDashboardData(10).ranking();

            assertThat(ranking).extracting(TollGateTrafficDto::getUnitName)
                    .doesNotContain("입구영업소");
        }

        @Test
        @DisplayName("ranking_excludesItemsWithNullOrBlankAmount")
        void ranking_excludesItemsWithNullOrBlankAmount() {
            stubIcApi(List.of(
                    icItem("A", "정상", "1", "5.0", "도공", "14"),
                    icItem("B", "null값", "1", null, "도공", "14"),
                    icItem("C", "공백", "1", "  ", "도공", "14")
            ));

            assertThat(trafficService.getDashboardData(10).ranking()).hasSize(1);
        }

        @Test
        @DisplayName("ranking_whenEmpty_returnsEmptyList")
        void ranking_whenEmpty_returnsEmptyList() {
            stubIcApiEmpty();

            assertThat(trafficService.getDashboardData(10).ranking()).isEmpty();
        }
    }

    // ================================================================
    // getDashboardData - summary
    // ================================================================

    @Nested
    @DisplayName("getDashboardData - summary")
    class Summary {

        @Test
        @DisplayName("summary_totalVolumeIsSumOfExitTraffic")
        void summary_totalVolumeIsSumOfExitTraffic() {
            stubIcApi(List.of(
                    icItem("A", "서울", "1", "10.0", "도공", "14"),
                    icItem("B", "부산", "1", "5.0", "도공", "14")
            ));

            NationwideTrafficDto summary = trafficService.getDashboardData(10).summary();

            assertThat(summary.getTotalVolume()).isEqualTo("15 대");
        }

        @Test
        @DisplayName("summary_busiestPlaceIsTopRankName")
        void summary_busiestPlaceIsTopRankName() {
            stubIcApi(List.of(
                    icItem("A", "서울", "1", "12.5", "도공", "14"),
                    icItem("B", "부산", "1", "5.0", "도공", "14")
            ));

            assertThat(trafficService.getDashboardData(10).summary().getBusiestPlace())
                    .isEqualTo("서울");
        }

        @Test
        @DisplayName("summary_busiestPlaceIsDashWhenNoData")
        void summary_busiestPlaceIsDashWhenNoData() {
            stubIcApiEmpty();

            assertThat(trafficService.getDashboardData(10).summary().getBusiestPlace())
                    .isEqualTo("-");
        }
    }

    // ================================================================
    // 혼잡도 분류
    // ================================================================

    @Nested
    @DisplayName("혼잡도 분류")
    class CongestionLevelTest {

        @Test
        @DisplayName("congestion_highWhenVolumeAtOrAboveHighThreshold")
        void congestion_highWhenVolumeAtOrAboveHighThreshold() {
            stubIcApi(List.of(icItem("A", "서울", "1", "5.0", "도공", "14")));

            TollGateTrafficDto top = trafficService.getDashboardData(1).ranking().get(0);

            assertThat(top.getCongestionLevel()).isEqualTo(CongestionLevel.HIGH);
            assertThat(top.getCongestionLabel()).isEqualTo("많음");
        }

        @Test
        @DisplayName("congestion_mediumWhenVolumeBetweenThresholds")
        void congestion_mediumWhenVolumeBetweenThresholds() {
            stubIcApi(List.of(icItem("A", "서울", "1", "3.0", "도공", "14")));

            TollGateTrafficDto top = trafficService.getDashboardData(1).ranking().get(0);

            assertThat(top.getCongestionLevel()).isEqualTo(CongestionLevel.MEDIUM);
            assertThat(top.getCongestionLabel()).isEqualTo("보통");
        }

        @Test
        @DisplayName("congestion_lowWhenVolumeBelowMediumThreshold")
        void congestion_lowWhenVolumeBelowMediumThreshold() {
            stubIcApi(List.of(icItem("A", "서울", "1", "1.5", "도공", "14")));

            TollGateTrafficDto top = trafficService.getDashboardData(1).ranking().get(0);

            assertThat(top.getCongestionLevel()).isEqualTo(CongestionLevel.LOW);
            assertThat(top.getCongestionLabel()).isEqualTo("적음");
        }
    }

    // ================================================================
    // 헬퍼
    // ================================================================

    private TrafficIcItem icItem(String code, String name, String inoutType,
                                              String amount, String exDivName, String sumTm) {
        TrafficIcItem item = new TrafficIcItem();
        item.setUnitCode(code);
        item.setUnitName(name);
        item.setInoutType(inoutType);
        item.setTrafficAmount(amount);
        item.setExDivName(exDivName);
        item.setSumTm(sumTm);
        return item;
    }

    private void stubIcApi(List<TrafficIcItem> items) {
        TrafficIcResponse response = new TrafficIcResponse();
        response.setCode("SUCCESS");
        response.setList(items);
        when(exApiClient.getTrafficIc(any(), any(), any(), any(), any(), any()))
                .thenReturn(response);
    }

    private void stubIcApiEmpty() {
        TrafficIcResponse response = new TrafficIcResponse();
        response.setCode("SUCCESS");
        response.setList(Collections.emptyList());
        when(exApiClient.getTrafficIc(any(), any(), any(), any(), any(), any()))
                .thenReturn(response);
    }

    private TrafficRegionItem regionItem(String regionCode, String regionName,
                                         String amount, String inoutType, String sumTm, String sumDate) {
        TrafficRegionItem item = new TrafficRegionItem();
        item.setRegionCode(regionCode);
        item.setRegionName(regionName);
        item.setTrafficAmount(amount);
        item.setInoutType(inoutType);
        item.setSumTm(sumTm);
        item.setSumDate(sumDate);
        return item;
    }

    private void stubRegionApi(List<TrafficRegionItem> items) {
        TrafficRegionResponse response = new TrafficRegionResponse();
        response.setCode("SUCCESS");
        response.setList(items);
        when(exApiClient.getTrafficRegion(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(response);
    }

    @Nested
    @DisplayName("getRegionRanking")
    class RegionRankingTest {

        @Test
        @DisplayName("regionRanking_aggregatesByRegionCode")
        void regionRanking_aggregatesByRegionCode() {
            stubRegionApi(List.of(
                    regionItem("927", "전북본부", "100", "1", "0900", "20260313"),
                    regionItem("927", "전북본부", "200", "0", "0900", "20260313"),
                    regionItem("905", "대구경북본부", "500", "1", "0900", "20260313")
            ));

            List<RegionTrafficDto> result = trafficService.getRegionRanking();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getRegionCode()).isEqualTo("905");
            assertThat(result.get(0).getExitVolume()).isEqualTo(500L);
            assertThat(result.get(1).getExitVolume()).isEqualTo(100L);
            assertThat(result.get(1).getEntranceVolume()).isEqualTo(200L);
        }

        @Test
        @DisplayName("regionRanking_assignsRankStartingFromOne")
        void regionRanking_assignsRankStartingFromOne() {
            stubRegionApi(List.of(
                    regionItem("905", "대구경북본부", "500", "1", "0900", "20260313"),
                    regionItem("927", "전북본부", "300", "1", "0900", "20260313")
            ));

            List<RegionTrafficDto> result = trafficService.getRegionRanking();

            assertThat(result).extracting(RegionTrafficDto::getRank).containsExactly(1, 2);
        }

        @Test
        @DisplayName("regionRanking_topRankHasBarWidth100")
        void regionRanking_topRankHasBarWidth100() {
            stubRegionApi(List.of(
                    regionItem("905", "대구경북본부", "500", "1", "0900", "20260313"),
                    regionItem("927", "전북본부", "250", "1", "0900", "20260313")
            ));

            List<RegionTrafficDto> result = trafficService.getRegionRanking();

            assertThat(result.get(0).getBarWidth()).isEqualTo(100);
            assertThat(result.get(1).getBarWidth()).isEqualTo(50);
        }

        @Test
        @DisplayName("regionRanking_whenApiFailureCode_returnsEmptyList")
        void regionRanking_whenApiFailureCode_returnsEmptyList() {
            TrafficRegionResponse response = new TrafficRegionResponse();
            response.setCode("99");
            when(exApiClient.getTrafficRegion(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(response);

            assertThat(trafficService.getRegionRanking()).isEmpty();
        }

        @Test
        @DisplayName("regionRanking_whenFeignThrows_returnsEmptyList")
        void regionRanking_whenFeignThrows_returnsEmptyList() {
            when(exApiClient.getTrafficRegion(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("timeout"));

            assertThat(trafficService.getRegionRanking()).isEmpty();
        }

        @Test
        @DisplayName("regionRanking_formatsVolume")
        void regionRanking_formatsVolume() {
            stubRegionApi(List.of(
                    regionItem("905", "대구경북본부", "1234", "1", "0900", "20260313")
            ));

            RegionTrafficDto dto = trafficService.getRegionRanking().get(0);

            assertThat(dto.getFormattedExitVolume()).isEqualTo("1,234 대");
        }
    }
}
