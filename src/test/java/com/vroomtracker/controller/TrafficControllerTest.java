package com.vroomtracker.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TrafficController 는 GET / 에서 HTML 껍데기만 서빙합니다.
 * 데이터는 TrafficApiController (GET /api/*) 가 담당하며,
 * 프론트엔드 JS가 각 섹션을 비동기로 채웁니다.
 */
@WebMvcTest(TrafficController.class)
class TrafficControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("index_returns200AndIndexView")
    void index_returns200AndIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("index_modelHasNoDataAttributes")
    void index_modelHasNoDataAttributes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(model().attributeDoesNotExist("summary"))
                .andExpect(model().attributeDoesNotExist("tollGates"))
                .andExpect(model().attributeDoesNotExist("hourlyPattern"));
    }
}
