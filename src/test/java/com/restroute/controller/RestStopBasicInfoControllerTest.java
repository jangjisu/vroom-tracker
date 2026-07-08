package com.restroute.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.restroute.controller.response.RestStopBasicInfoResponse;
import com.restroute.service.RestStopBasicInfoQueryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RestStopBasicInfoControllerTest {

    @Mock
    private RestStopBasicInfoQueryService restStopBasicInfoQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RestStopBasicInfoController(restStopBasicInfoQueryService))
                .build();
    }

    @Test
    @DisplayName("GET /api/rest-stops/{serviceAreaCode}/basic-info는 휴게소 기본정보를 ApiResponse로 반환한다")
    void getRestStopBasicInfo_returnsBasicInfo() throws Exception {
        RestStopBasicInfoResponse response = new RestStopBasicInfoResponse(
                "A00001",
                "001",
                "서울만남(부산)휴게소",
                "0010",
                "경부선",
                "127.042514",
                "37.459939",
                "000001",
                "경기 성남시",
                "02-573-7430",
                "투썸플레이스");
        when(restStopBasicInfoQueryService.findByServiceAreaCode("A00001")).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/rest-stops/A00001/basic-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.serviceAreaCode").value("A00001"))
                .andExpect(jsonPath("$.data.unitCode").value("001"))
                .andExpect(jsonPath("$.data.unitName").value("서울만남(부산)휴게소"))
                .andExpect(jsonPath("$.data.routeNo").value("0010"))
                .andExpect(jsonPath("$.data.routeName").value("경부선"))
                .andExpect(jsonPath("$.data.xValue").value("127.042514"))
                .andExpect(jsonPath("$.data.yValue").value("37.459939"))
                .andExpect(jsonPath("$.data.stdRestCd").value("000001"))
                .andExpect(jsonPath("$.data.address").value("경기 성남시"))
                .andExpect(jsonPath("$.data.telNo").value("02-573-7430"))
                .andExpect(jsonPath("$.data.brand").value("투썸플레이스"));
    }

    @Test
    @DisplayName("GET /api/rest-stops/{serviceAreaCode}/basic-info는 대상 휴게소가 없으면 NOT_FOUND를 반환한다")
    void getRestStopBasicInfo_returnsNotFoundWhenRestStopMissing() throws Exception {
        when(restStopBasicInfoQueryService.findByServiceAreaCode("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rest-stops/UNKNOWN/basic-info"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }
}
