package com.restroute.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.restroute.controller.response.RestStopFacilityResponse;
import com.restroute.service.RestStopFacilityQueryService;
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
class RestStopFacilityControllerTest {

    @Mock
    private RestStopFacilityQueryService restStopFacilityQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RestStopFacilityController(restStopFacilityQueryService))
                .build();
    }

    @Test
    @DisplayName("GET /api/rest-stops/{serviceAreaCode}/facilities는 시설/주차 정보를 ApiResponse로 반환한다")
    void getRestStopFacilities_returnsFacilities() throws Exception {
        RestStopFacilityResponse response = new RestStopFacilityResponse("수유실|쉼터", "O", "X", "하행", 15, 27, 1);
        when(restStopFacilityQueryService.findByServiceAreaCode("A00001")).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/rest-stops/A00001/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.convenience").value("수유실|쉼터"))
                .andExpect(jsonPath("$.data.maintenanceYn").value("O"))
                .andExpect(jsonPath("$.data.truckSaYn").value("X"))
                .andExpect(jsonPath("$.data.direction").value("하행"))
                .andExpect(jsonPath("$.data.compactCarParkingCount").value(15))
                .andExpect(jsonPath("$.data.fullSizeCarParkingCount").value(27))
                .andExpect(jsonPath("$.data.disabledParkingCount").value(1));
    }

    @Test
    @DisplayName("GET /api/rest-stops/{serviceAreaCode}/facilities는 대상 휴게소가 없으면 NOT_FOUND를 반환한다")
    void getRestStopFacilities_returnsNotFoundWhenRestStopMissing() throws Exception {
        when(restStopFacilityQueryService.findByServiceAreaCode("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rest-stops/UNKNOWN/facilities"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }
}
