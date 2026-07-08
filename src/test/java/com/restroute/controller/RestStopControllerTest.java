package com.restroute.controller;

import static com.restroute.support.RestStopTestFixtures.restStopItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.restroute.controller.response.FoodMenuItemResponse;
import com.restroute.controller.response.FoodMenuResponse;
import com.restroute.controller.response.FoodMenuSectionResponse;
import com.restroute.controller.response.OilInfoResponse;
import com.restroute.controller.response.OilStationConvenienceResponse;
import com.restroute.controller.response.RestStopDetailViewResponse;
import com.restroute.domain.RestStopEntity;
import com.restroute.service.RestStopQueryService;
import java.time.LocalDateTime;
import java.util.List;
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

    @Test
    @DisplayName("GET /api/rest-stops/{serviceAreaCode}는 휴게소 상세 정보를 ApiResponse로 반환한다")
    void getRestStopDetail_returnsRestStopDetail() throws Exception {
        RestStopDetailViewResponse response = new RestStopDetailViewResponse(
                "A00001",
                "서울만남(부산)휴게소",
                "경부선",
                "127.042514",
                "37.459939",
                "경기 성남시",
                "투썸플레이스",
                "수유실",
                "O",
                "X",
                "하행",
                15,
                27,
                1,
                new OilInfoResponse(
                        "AD",
                        "1,999원",
                        "1,997원",
                        "1,157원",
                        "02-573-7430",
                        LocalDateTime.of(2026, 6, 16, 7, 30),
                        List.of(new OilStationConvenienceResponse("00:00", "24:00", "쉼터", "고객쉼터"))),
                new FoodMenuResponse(
                        List.of(new FoodMenuItemResponse("농심어묵우동", "7000", "시원한 우동", true, true, true, "S", "여름")),
                        List.of(new FoodMenuSectionResponse(
                                "recommended",
                                "추천 메뉴",
                                List.of(new FoodMenuItemResponse(
                                        "농심어묵우동", "7000", "시원한 우동", true, true, true, "S", "여름"))))));
        when(restStopQueryService.findDetailByServiceAreaCode("A00001")).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/rest-stops/A00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.serviceAreaCode").value("A00001"))
                .andExpect(jsonPath("$.data.restStopName").value("서울만남(부산)휴게소"))
                .andExpect(jsonPath("$.data.routeName").value("경부선"))
                .andExpect(jsonPath("$.data.xValue").value("127.042514"))
                .andExpect(jsonPath("$.data.yValue").value("37.459939"))
                .andExpect(jsonPath("$.data.address").value("경기 성남시"))
                .andExpect(jsonPath("$.data.brand").value("투썸플레이스"))
                .andExpect(jsonPath("$.data.convenience").value("수유실"))
                .andExpect(jsonPath("$.data.maintenanceYn").value("O"))
                .andExpect(jsonPath("$.data.truckSaYn").value("X"))
                .andExpect(jsonPath("$.data.direction").value("하행"))
                .andExpect(jsonPath("$.data.compactCarParkingCount").value(15))
                .andExpect(jsonPath("$.data.fullSizeCarParkingCount").value(27))
                .andExpect(jsonPath("$.data.disabledParkingCount").value(1))
                .andExpect(jsonPath("$.data.oilInfo.oilCompany").value("AD"))
                .andExpect(jsonPath("$.data.oilInfo.gasolinePrice").value("1,999원"))
                .andExpect(jsonPath("$.data.oilInfo.dieselPrice").value("1,997원"))
                .andExpect(jsonPath("$.data.oilInfo.lpgPrice").value("1,157원"))
                .andExpect(jsonPath("$.data.oilInfo.telNo").value("02-573-7430"))
                .andExpect(jsonPath("$.data.oilInfo.lastRefreshedAt").value("2026-06-16T07:30:00"))
                .andExpect(jsonPath("$.data.oilInfo.oilStationConveniences[0].startTime")
                        .value("00:00"))
                .andExpect(jsonPath("$.data.oilInfo.oilStationConveniences[0].endTime")
                        .value("24:00"))
                .andExpect(jsonPath("$.data.oilInfo.oilStationConveniences[0].name")
                        .value("쉼터"))
                .andExpect(jsonPath("$.data.oilInfo.oilStationConveniences[0].description")
                        .value("고객쉼터"))
                .andExpect(jsonPath("$.data.foodMenu.menus[0].foodName").value("농심어묵우동"))
                .andExpect(jsonPath("$.data.foodMenu.menus[0].representative").value(true))
                .andExpect(jsonPath("$.data.foodMenu.menus[0].bestFood").value(true))
                .andExpect(jsonPath("$.data.foodMenu.menus[0].premium").value(true))
                .andExpect(jsonPath("$.data.foodMenu.menus[0].season").value("S"))
                .andExpect(jsonPath("$.data.foodMenu.menus[0].seasonLabel").value("여름"))
                .andExpect(jsonPath("$.data.foodMenu.sections[0].key").value("recommended"))
                .andExpect(jsonPath("$.data.foodMenu.sections[0].title").value("추천 메뉴"))
                .andExpect(jsonPath("$.data.foodMenu.sections[0].menus[0].foodName")
                        .value("농심어묵우동"));
    }

    @Test
    @DisplayName("GET /api/rest-stops/{serviceAreaCode}는 대상 휴게소가 없으면 NOT_FOUND를 반환한다")
    void getRestStopDetail_returnsNotFoundWhenRestStopMissing() throws Exception {
        when(restStopQueryService.findDetailByServiceAreaCode("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rest-stops/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }
}
