package com.vroomtracker.controller;

import static com.vroomtracker.support.RestStopTestFixtures.restStopItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.service.RestStopQueryService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RestStopControllerTest {

    @Mock
    private RestStopQueryService restStopQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RestStopController(restStopQueryService))
                .build();
    }

    @Test
    @DisplayName("GET /api/rest-stops는 휴게소 목록을 ApiResponse로 반환한다")
    void getRestStops_returnsRestStops() throws Exception {
        RestStopEntity restStop = RestStopEntity.from(restStopItem("001", "서울만남(부산)휴게소"));
        when(restStopQueryService.findAll()).thenReturn(List.of(restStop));

        mockMvc.perform(get("/api/rest-stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data[0].unitCode").value("001"))
                .andExpect(jsonPath("$.data[0].unitName").value("서울만남(부산)휴게소"))
                .andExpect(jsonPath("$.data[0].routeNo").value("0010"))
                .andExpect(jsonPath("$.data[0].routeName").value("경부선"))
                .andExpect(jsonPath("$.data[0].xValue").value("127.042514"))
                .andExpect(jsonPath("$.data[0].yValue").value("37.459939"))
                .andExpect(jsonPath("$.data[0].stdRestCd").value("000001"))
                .andExpect(jsonPath("$.data[0].serviceAreaCode").value("A00001"));
    }
}
