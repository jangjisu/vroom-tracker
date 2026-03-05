package com.vroomtracker.controller;

import com.vroomtracker.dto.NationwideTrafficDto;
import com.vroomtracker.dto.TollGateTrafficDto;
import com.vroomtracker.dto.TrafficFlowDto;
import com.vroomtracker.service.TrafficService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrafficController.class)
class TrafficControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrafficService trafficService;

    @Test
    @DisplayName("index_returns200AndIndexView")
    void index_returns200AndIndexView() throws Exception {
        when(trafficService.getDashboardData(anyInt()))
                .thenReturn(dashboardData());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("index_modelContainsRequiredAttributes")
    void index_modelContainsRequiredAttributes() throws Exception {
        when(trafficService.getDashboardData(anyInt()))
                .thenReturn(dashboardData());

        mockMvc.perform(get("/"))
                .andExpect(model().attributeExists("summary"))
                .andExpect(model().attributeExists("tollGates"))
                .andExpect(model().attributeExists("hourlyPattern"))
                .andExpect(model().attributeExists("pageLoadTime"));
    }

    @Test
    @DisplayName("index_doesNotThrow500WhenDataIsEmpty")
    void index_doesNotThrow500WhenDataIsEmpty() throws Exception {
        when(trafficService.getDashboardData(anyInt()))
                .thenReturn(new TrafficService.DashboardData(
                        NationwideTrafficDto.builder()
                                .totalVolume("0.0 만대").congestedSections(0)
                                .busiestPlace("-").busiestVolume("0").build(),
                        Collections.emptyList(),
                        Collections.emptyList()
                ));

        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    private TrafficService.DashboardData dashboardData() {
        return new TrafficService.DashboardData(
                NationwideTrafficDto.builder()
                        .totalVolume("25.5 만대").sumTm("2024-03-05 14시")
                        .congestedSections(3).busiestPlace("서울").busiestVolume("12.5 만대")
                        .build(),
                List.of(TollGateTrafficDto.builder()
                        .rank(1).unitCode("0010A").unitName("서울").exDivName("도공")
                        .exitVolume(12.5).formattedVolume("12.5 만대").sumTm("14시")
                        .congestionLevel("HIGH").congestionLabel("많음").barWidth(100)
                        .build()),
                List.of(TrafficFlowDto.builder()
                        .sphlDfttNm("평일").sphlDfttScopTypeNm("당일").stdHour("14").trfl("1000")
                        .build())
        );
    }
}
