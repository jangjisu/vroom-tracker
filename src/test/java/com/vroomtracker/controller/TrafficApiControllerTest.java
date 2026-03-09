package com.vroomtracker.controller;

import com.vroomtracker.dto.DashboardData;
import com.vroomtracker.dto.NationwideTrafficDto;
import com.vroomtracker.dto.TollGateTrafficDto;
import com.vroomtracker.dto.TrafficFlowDto;
import com.vroomtracker.service.TrafficFlowService;
import com.vroomtracker.service.TrafficService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrafficApiController.class)
class TrafficApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrafficService trafficService;

    @MockBean
    private TrafficFlowService trafficFlowService;

    @Test
    @DisplayName("GET /api/summary returns 200 with ApiResponse wrapper")
    void getSummary_returns200WithApiResponseWrapper() throws Exception {
        NationwideTrafficDto summary = NationwideTrafficDto.builder()
                .totalVolume("100.0 만대")
                .sumTm("2024-01-01 14시")
                .congestedSections(3)
                .busiestPlace("서울")
                .busiestVolume("12.5 만대")
                .build();
        DashboardData dashboardData = new DashboardData(summary, List.of());
        when(trafficService.getDashboardData(anyInt())).thenReturn(dashboardData);

        mockMvc.perform(get("/api/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalVolume").value("100.0 만대"))
                .andExpect(jsonPath("$.data.busiestPlace").value("서울"));
    }

    @Test
    @DisplayName("GET /api/ranking returns 200 with ApiResponse wrapper")
    void getRanking_returns200WithApiResponseWrapper() throws Exception {
        DashboardData dashboardData = new DashboardData(
                NationwideTrafficDto.builder().totalVolume("0").sumTm("").congestedSections(0).busiestPlace("-").busiestVolume("0").build(),
                List.of()
        );
        when(trafficService.getDashboardData(anyInt())).thenReturn(dashboardData);

        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/hourly-pattern returns 200 with ApiResponse wrapper")
    void getHourlyPattern_returns200WithApiResponseWrapper() throws Exception {
        TrafficFlowDto dto = TrafficFlowDto.builder()
                .dayType("평일")
                .periodRange("당일")
                .hour("14")
                .vehicleCount("1000")
                .formattedVehicleCount("1,000 대")
                .build();
        when(trafficFlowService.findByYear(anyString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/hourly-pattern"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].dayType").value("평일"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler returns 500 on unexpected exception")
    void globalExceptionHandler_returns500OnException() throws Exception {
        when(trafficService.getDashboardData(anyInt())).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get("/api/summary"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }
}
